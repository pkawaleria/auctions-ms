package pl.kawaleria.auctsys.auctions.domain

import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.geo.Circle
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Metrics
import org.springframework.data.geo.Point
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.security.core.Authentication
import pl.kawaleria.auctsys.auctions.dto.exceptions.*
import pl.kawaleria.auctsys.auctions.dto.requests.AuctionsSearchRequest
import pl.kawaleria.auctsys.auctions.dto.requests.CreateAuctionRequest
import pl.kawaleria.auctsys.auctions.dto.requests.UpdateAuctionRequest
import pl.kawaleria.auctsys.auctions.dto.responses.AuctionDetailedResponse
import pl.kawaleria.auctsys.auctions.dto.responses.PagedAuctions
import pl.kawaleria.auctsys.auctions.dto.responses.toDetailedResponse
import pl.kawaleria.auctsys.auctions.dto.responses.toPagedAuctions
import pl.kawaleria.auctsys.categories.domain.CategoryFacade
import pl.kawaleria.auctsys.categories.dto.response.CategoryNameResponse
import pl.kawaleria.auctsys.categories.dto.response.CategoryPathResponse
import pl.kawaleria.auctsys.configs.SecurityHelper
import pl.kawaleria.auctsys.verifications.ContentVerificationClient
import pl.kawaleria.auctsys.verifications.TextRequest
import java.time.Clock
import java.time.Duration
import java.time.Instant

class AuctionFacade(
    private val auctionRepository: AuctionRepository,
    private val cityRepository: CityRepository,
    private val auctionSearchRepository: AuctionSearchRepository,
    private val contentVerificationClient: ContentVerificationClient,
    private val categoryFacade: CategoryFacade,
    private val auctionRules: AuctionRules,
    private val radiusRules: RadiusRules,
    private val auctionCategoryDeleter: AuctionCategoryDeleter,
    private val securityHelper: SecurityHelper,
    private val clock: Clock
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun create(createRequest: CreateAuctionRequest, auctioneerId: String): Auction {
        if (!validateCreateAuctionRequest(createRequest)) throw InvalidAuctionCreationRequestException()
        verifyAuctionContent(createRequest.name, createRequest.description)
        val categoryPath: CategoryPath =
            categoryFacade.getFullCategoryPath(createRequest.categoryId).toAuctionCategoryPathModel()

        val city = cityRepository.findById(createRequest.cityId).orElseThrow { CityNotFoundException() }

        val auction = Auction(
            name = createRequest.name,
            description = createRequest.description,
            price = createRequest.price,
            auctioneerId = auctioneerId,
            thumbnail = byteArrayOf(),
            expiresAt = newExpirationInstant(),
            cityId = createRequest.cityId,
            category = categoryPath.lastCategory(),
            categoryPath = categoryPath,
            productCondition = createRequest.productCondition,
            cityName = city.name,
            location = GeoJsonPoint(city.latitude, city.longitude)
        )

        return auctionRepository.save(auction)
    }

    private fun validateCreateAuctionRequest(payload: CreateAuctionRequest): Boolean {
        val validatedName: Boolean = validateName(payload.name)
        val validatedDescription: Boolean = validateDescription(payload.description)
        val validatedPrice: Boolean = validatePrice(payload.price)

        return (validatedName && validatedDescription && validatedPrice)
    }

    private fun validateName(name: String): Boolean {
        val regex: Regex = "^[a-zA-Z0-9 .]*$".toRegex()

        return name.isNotEmpty() && name.length in 5..100 && regex.matches(name)
    }

    private fun validateDescription(description: String): Boolean {
        val regex: Regex = "^[a-zA-Z0-9 .]*$".toRegex()

        return description.isNotEmpty() && description.length in 20..500 && regex.matches(description)
    }

    private fun validatePrice(price: Double): Boolean = price > 0

    private fun verifyAuctionContent(name: String, description: String) {
        var foundInappropriateContent = false
        val textToVerify = TextRequest("$name $description")

        try {
            foundInappropriateContent = contentVerificationClient.verifyText(textToVerify).isInappropriate
        } catch (e: Exception) {
            logger.error("Error during name and description verification", e)
        }

        if (foundInappropriateContent) {
            logger.info("Found explicit content")
            throw InappropriateContentException()
        } else {
            logger.info("Auction name and description verified positively")
        }
    }

    fun findAuctionsByAuctioneer(auctioneerId: String): MutableList<Auction> =
        auctionRepository.findAuctionsByAuctioneerId(auctioneerId)

    fun findAuctionById(id: String): Auction = auctionRepository.findById(id).orElseThrow { AuctionNotFoundException() }

    fun searchAuctions(searchRequest: AuctionsSearchRequest, pageRequest: PageRequest): PagedAuctions {
        validateGeolocationFiltersIfExist(searchRequest.radius, searchRequest.cityId)
        val query: Query = buildSearchQuery(searchRequest, pageRequest)
        return auctionSearchRepository.search(query, pageRequest).toPagedAuctions()
    }

    private fun validateGeolocationFiltersIfExist(radius: Double?, cityId: String?) {
        if (radius == null) return

        if (radius !in radiusRules.min..radiusRules.max) throw SearchRadiusOutOfBoundsException()
        if (cityId == null) throw SearchRadiusWithoutCityException()
    }

    private fun buildSearchQuery(searchRequest: AuctionsSearchRequest, pageRequest: PageRequest): Query {
        val query = Query()
        searchRequest.searchPhrase?.takeIf { it.isNotBlank() }?.let {
            query.addCriteria(Criteria.where("name").regex(it, "i"))
        }
        searchRequest.categoryName?.takeIf { it.isNotBlank() }?.let {
            query.addCriteria(Criteria.where("categoryPath.pathElements.name").regex(searchRequest.categoryName, "i"))
        }
        searchRequest.cityId?.takeIf{ it.isNotBlank() }?.let { cityId ->
            val city: City = cityRepository.findById(cityId).orElseThrow { SearchRadiusWithoutCityException() }

            if (searchRequest.radius != null && searchRequest.radius > 0) {
                val point = Point(city.latitude, city.longitude)
                val distance = Distance(searchRequest.radius, Metrics.KILOMETERS)
                val circle = Circle(point, distance)
                query.addCriteria(Criteria.where("location").withinSphere(circle))
            } else {
                query.addCriteria(Criteria.where("cityId").isEqualTo(cityId))
            }
        }

        query.with(pageRequest)
        return query
    }

    fun update(id: String, payload: UpdateAuctionRequest, authContext: Authentication): Auction {
        if (!validateUpdateAuctionRequest(payload)) throw InvalidAuctionUpdateRequestException()

        val auction: Auction = findAuctionById(id)
        securityHelper.assertUserIsAuthorizedForResource(authContext, auction.auctioneerId)

        cityRepository.findById(payload.cityId).orElseThrow { CityNotFoundException() }

        auction.name = payload.name
        auction.price = payload.price
        auction.description = payload.description
        auction.productCondition = payload.productCondition
        auction.cityId = payload.cityId
        auction.cityName = payload.cityName
        auction.location = payload.location

        return auctionRepository.save(auction)
    }

    private fun validateUpdateAuctionRequest(payload: UpdateAuctionRequest): Boolean {
        val validatedName: Boolean = validateName(payload.name)
        val validatedDescription: Boolean = validateDescription(payload.description)
        val validatedPrice: Boolean = validatePrice(payload.price)

        return (validatedName && validatedDescription && validatedPrice)
    }

    fun delete(auctionId: String, authContext: Authentication) {
        val auctionToDelete: Auction = findAuctionById(auctionId)
        securityHelper.assertUserIsAuthorizedForResource(authContext, auctionToDelete.auctioneerId)
        auctionRepository.delete(auctionToDelete)
    }

    fun changeCategory(auctionId: String, categoryId: String, authContext: Authentication): AuctionDetailedResponse {
        val auction: Auction = findAuctionById(auctionId)
        securityHelper.assertUserIsAuthorizedForResource(authContext, auction.auctioneerId)
        val pathDto: CategoryPathResponse = categoryFacade.getFullCategoryPath(categoryId)
        val path: CategoryPath = pathDto.toAuctionCategoryPathModel()
        auction.assignPath(path)

        return auctionRepository.save(auction).toDetailedResponse()
    }

    fun eraseCategoryFromAuctions(categoryName: String): Unit =
        auctionCategoryDeleter.eraseCategoryFromAuctions(categoryName)

    fun saveThumbnail(auctionId: String, byteArray: ByteArray) {
        val auction: Auction = findAuctionById(auctionId)
        auction.thumbnail = byteArray
        auctionRepository.save(auction)
    }

    fun accept(auctionId: String) {
        val auction: Auction = findAuctionById(auctionId)
        auction.accept()
        auctionRepository.save(auction)
    }

    fun archive(auctionId: String, authContext: Authentication) {
        val auction: Auction = findAuctionById(auctionId)
        securityHelper.assertUserIsAuthorizedForResource(authContext, auction.auctioneerId)
        auction.archive()
        auctionRepository.save(auction)
    }

    fun reject(auctionId: String) {
        val auction: Auction = findAuctionById(auctionId)
        auction.reject()
        auctionRepository.save(auction)
    }

    fun findRejectedAuctions(auctioneerId: String, pageable: Pageable): PagedAuctions =
        auctionRepository.findRejectedAuctions(auctioneerId, pageable).toPagedAuctions()

    fun findActiveAuctions(auctioneerId: String, pageable: Pageable): PagedAuctions =
        auctionRepository.findAcceptedAuctions(auctioneerId, pageable).toPagedAuctions()

    fun findExpiredAuctions(auctioneerId: String, pageable: Pageable): PagedAuctions =
        auctionRepository.findExpiredAuctions(Instant.now(clock), auctioneerId, pageable).toPagedAuctions()

    fun findArchivedAuctions(auctioneerId: String, pageable: Pageable): PagedAuctions =
        auctionRepository.findArchivedAuctions(auctioneerId, pageable).toPagedAuctions()

    fun findAwaitingAcceptanceAuctions(auctioneerId: String, pageable: Pageable): PagedAuctions =
        auctionRepository.findAwaitingAcceptanceAuctions(auctioneerId, pageable).toPagedAuctions()

    private fun newExpirationInstant(): Instant {
        val daysToExpire: Long = auctionRules.days.toLong()
        return Instant.now(clock).plusSeconds(Duration.ofDays(daysToExpire).toSeconds())
    }

    // for tests
    fun saveCity(city: City): City {
        return cityRepository.save(city)
    }
}

private fun CategoryPathResponse.toAuctionCategoryPathModel(): CategoryPath {
    return CategoryPath(this.path.map { it.toAuctionCategoryModel() }.toMutableList())
}

private fun CategoryNameResponse.toAuctionCategoryModel(): Category {
    return Category(this.id, this.name)
}