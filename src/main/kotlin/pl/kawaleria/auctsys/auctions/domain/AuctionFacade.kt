package pl.kawaleria.auctsys.auctions.domain

import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Metrics
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
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
import kotlin.math.abs

class AuctionFacade(private val auctionRepository: AuctionRepository,
                    private val cityRepository: CityRepository,
                    private val contentVerificationClient: ContentVerificationClient,
                    private val categoryFacade: CategoryFacade,
                    private val auctionRules: AuctionRules,
                    private val radiusRules: RadiusRules,
                    private val auctionCategoryDeleter: AuctionCategoryDeleter,
                    private val securityHelper: SecurityHelper,
                    private val clock: Clock) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun findAuctionsByAuctioneer(auctioneerId: String): MutableList<Auction> = auctionRepository.findAuctionsByAuctioneerId(auctioneerId)

    fun findAuctionById(id: String): Auction = auctionRepository.findById(id).orElseThrow { AuctionNotFoundException() }

    fun changeCategory(auctionId: String, categoryId: String, authContext: Authentication): AuctionDetailedResponse {
        val auction: Auction = findAuctionById(auctionId)
        securityHelper.assertUserIsAuthorizedForResource(authContext, auction.auctioneerId)
        val pathDto: CategoryPathResponse = categoryFacade.getFullCategoryPath(categoryId)
        val path: CategoryPath = pathDto.toAuctionCategoryPathModel()
        auction.assignPath(path)

        return auctionRepository.save(auction).toDetailedResponse()
    }

    fun create(createRequest: CreateAuctionRequest, auctioneerId: String): Auction {
        if (!validateCreateAuctionRequest(createRequest)) throw InvalidAuctionCreationRequestException()
        verifyAuctionContent(createRequest.name, createRequest.description)
        val categoryPath: CategoryPath = categoryFacade.getFullCategoryPath(createRequest.categoryId).toAuctionCategoryPathModel()

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
                cityName = createRequest.cityName,
                location = createRequest.location,
        )

        return auctionRepository.save(auction)
    }

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

    fun update(id: String, payload: UpdateAuctionRequest, authContext: Authentication): Auction {
        if (!validateUpdateAuctionRequest(payload)) throw InvalidAuctionUpdateRequestException()

        val auction: Auction = findAuctionById(id)
        securityHelper.assertUserIsAuthorizedForResource(authContext, auction.auctioneerId)

        auction.name = payload.name
        auction.price = payload.price
        auction.description = payload.description
        auction.productCondition = payload.productCondition
        auction.cityId = payload.cityId
        auction.cityName = payload.cityName
        auction.location = payload.location

        return auctionRepository.save(auction)
    }

    fun delete(auctionId: String, authContext: Authentication) {
        val auctionToDelete = findAuctionById(auctionId)
        securityHelper.assertUserIsAuthorizedForResource(authContext, auctionToDelete.auctioneerId)
        auctionRepository.delete(auctionToDelete)
    }

    fun eraseCategoryFromAuctions(categoryName: String): Unit = auctionCategoryDeleter.eraseCategoryFromAuctions(categoryName)

    fun saveThumbnail(auctionId: String, byteArray: ByteArray) {
        val auction: Auction = findAuctionById(auctionId)
        auction.thumbnail = byteArray
        auctionRepository.save(auction)
    }

    fun searchAuctions(searchRequest: AuctionsSearchRequest, pageRequest: PageRequest): PagedAuctions {
        val categoryName: String? = searchRequest.categoryName
        val searchPhrase: String? = searchRequest.searchPhrase?.takeIf { it.isNotBlank() }
        val cityId: String? = searchRequest.cityId
        val radius: Double? = searchRequest.radius

        if (radius != null && radius !in radiusRules.min .. radiusRules.max) throw SearchRadiusOutOfBoundsException()
        if (cityId == null && radius != null) throw SearchRadiusWithoutCityException()

        // chyba nie potrzebne skoro w 122 linii jezeli radius jest poza przedzialem to wyjatek
        // if (radius != null && radius < 0) radius = abs(radius)

        return when {
            cityId != null && radius != null && (radius in radiusRules.min ..radiusRules.max) -> {
                val city: City = cityRepository.findById(cityId).get()
                val latitude: Double = city.latitude
                val longitude: Double = city.longitude

                val startPoint = GeoJsonPoint(latitude, longitude)
                val distance = Distance(radius, Metrics.KILOMETERS)
                auctionRepository.findByLocationNear(startPoint, distance, pageRequest)
            }

            cityId != null && (radius == null || radius == 0.0) ->
                auctionRepository.findAuctionsByCityId(cityId, pageRequest)

            cityId == null && searchPhrase != null && categoryName != null ->
                auctionRepository.findByNameContainingIgnoreCaseAndCategoryPathContaining(searchPhrase, categoryName, pageRequest)

            searchPhrase == null && categoryName != null ->
                auctionRepository.findAuctionsWithCategoryInPath(categoryName, pageRequest)

            searchPhrase != null && categoryName == null ->
                auctionRepository.findByNameContainingIgnoreCase(searchPhrase, pageRequest)

            else -> auctionRepository.findAll(pageRequest)
        }.toPagedAuctions()
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

    fun findRejectedAuctions(auctioneerId: String, pageable: Pageable) =
        auctionRepository.findRejectedAuctions(auctioneerId, pageable).toPagedAuctions()

    fun findActiveAuctions(auctioneerId: String, pageable: Pageable) =
        auctionRepository.findAcceptedAuctions(auctioneerId, pageable).toPagedAuctions()

    fun findExpiredAuctions(auctioneerId: String, pageable: Pageable) =
        auctionRepository.findExpiredAuctions(Instant.now(clock), auctioneerId, pageable).toPagedAuctions()

    fun findArchivedAuctions(auctioneerId: String, pageable: Pageable) =
        auctionRepository.findArchivedAuctions(auctioneerId, pageable).toPagedAuctions()

    fun findAwaitingAcceptanceAuctions(auctioneerId: String, pageable: Pageable) =
        auctionRepository.findAwaitingAcceptanceAuctions(auctioneerId, pageable).toPagedAuctions()

    private fun newExpirationInstant(): Instant {
        val daysToExpire: Long = auctionRules.days.toLong()
        return Instant.now(clock).plusSeconds(Duration.ofDays(daysToExpire).toSeconds())
    }

    private fun validateCreateAuctionRequest(payload: CreateAuctionRequest): Boolean {
        val validatedName: Boolean = validateName(payload.name)
        val validatedDescription: Boolean = validateDescription(payload.description)
        val validatedPrice: Boolean = validatePrice(payload.price)

        return (validatedName && validatedDescription && validatedPrice)
    }

    private fun validateUpdateAuctionRequest(payload: UpdateAuctionRequest): Boolean {
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
}

private fun CategoryPathResponse.toAuctionCategoryPathModel(): CategoryPath {
    return CategoryPath(this.path.map { it.toAuctionCategoryModel() }.toMutableList())
}

private fun CategoryNameResponse.toAuctionCategoryModel(): Category {
    return Category(this.id, this.name)
}