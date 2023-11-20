package pl.kawaleria.auctsys.auctions.domain

import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
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
import pl.kawaleria.auctsys.auctions.dto.events.AuctionViewedEvent
import pl.kawaleria.auctsys.auctions.dto.exceptions.*
import pl.kawaleria.auctsys.auctions.dto.requests.AuctionsSearchRequest
import pl.kawaleria.auctsys.auctions.dto.requests.CreateAuctionRequest
import pl.kawaleria.auctsys.auctions.dto.requests.UpdateAuctionRequest
import pl.kawaleria.auctsys.auctions.dto.responses.*
import pl.kawaleria.auctsys.categories.domain.CategoryFacade
import pl.kawaleria.auctsys.categories.dto.responses.CategoryNameResponse
import pl.kawaleria.auctsys.categories.dto.responses.CategoryPathResponse
import pl.kawaleria.auctsys.commons.SecurityHelper
import pl.kawaleria.auctsys.verifications.ContentVerificationClient
import pl.kawaleria.auctsys.verifications.TextRequest
import pl.kawaleria.auctsys.views.domain.AuctionViewsQueryFacade
import java.time.Clock
import java.time.Duration
import java.time.Instant

class AuctionFacade(
    private val auctionRepository: AuctionRepository,
    private val cityRepository: CityRepository,
    private val auctionSearchRepository: AuctionSearchRepository,
    private val contentVerificationClient: ContentVerificationClient,
    private val categoryFacade: CategoryFacade,
    private val auctionCreationRules: AuctionCreationRules,
    private val auctionSearchingRules: AuctionSearchingRules,
    private val auctionVerificationRules: AuctionVerificationRules,
    private val auctionCategoryDeleter: AuctionCategoryDeleter,
    private val auctionEventPublisher: AuctionDomainEventPublisher,
    private val auctionViewsQueryFacade: AuctionViewsQueryFacade,
    private val securityHelper: SecurityHelper,
    private val clock: Clock
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun create(createRequest: CreateAuctionRequest, auctioneerId: String): AuctionDetailedResponse {
        if (!validateCreateAuctionRequest(createRequest)) {
            throw InvalidAuctionCreationRequestException()
        }
        verifyAuctionContent(createRequest.name, createRequest.description)
        val categoryPath: CategoryPath =
            categoryFacade.getFullCategoryPath(createRequest.categoryId).toAuctionCategoryPathModel()

        val city: City = cityRepository.findById(createRequest.cityId).orElseThrow { CityNotFoundException() }

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
            province = city.province,
            location = GeoJsonPoint(city.longitude, city.latitude),
            phoneNumber = createRequest.phoneNumber
        )

        return auctionRepository.save(auction).toDetailedResponse()
    }

    private fun validateCreateAuctionRequest(payload: CreateAuctionRequest): Boolean {
        // TODO: Replace with spring-boot-starter-validation
//        val validatedName: Boolean = validateName(payload.name)
//        val validatedDescription: Boolean = validateDescription(payload.description)
//        val validatedPrice: Boolean = validatePrice(payload.price)
//
//        return (validatedName && validatedDescription && validatedPrice)
        return true;
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
        if (!auctionVerificationRules.enabled) {
            logger.debug("Auction text properties verification is switched off and will be omitted")
            return
        }

        val textToVerify = TextRequest("$name $description")
        val foundInappropriateContent: Boolean = contentVerificationClient.verifyText(textToVerify).isInappropriate
        if (foundInappropriateContent) {
            logger.info("Found explicit content")
            throw InappropriateContentException()
        } else {
            logger.info("Auction name and description verified positively")
        }
    }

    fun findAuctionsByAuctioneer(auctioneerId: String): List<AuctionSimplifiedResponse> =
        auctionRepository.findActiveAuctionsByAuctioneerId(auctioneerId, Instant.now(clock)).map{ it.toSimplifiedResponse() }
    fun findAuctionsByAuctioneer(auctioneerId: String, pageable: Pageable): PagedAuctions =
        auctionRepository.findAuctionsByAuctioneerId(auctioneerId, pageable).toPagedAuctions()

    fun getAuctionDetails(id: String, ipAddress : String): AuctionDetailedResponse {
        val auction: Auction = findActiveAuctionById(id)
        auctionEventPublisher.publishAuctionView(auctionViewedEvent = AuctionViewedEvent(ipAddress, id))
        val views: Long = auctionViewsQueryFacade.getAuctionViews(auctionId = id)
        return auction.toDetailedResponse(viewCount = views)
    }

    private fun findActiveAuctionById(id: String): Auction = auctionRepository.findActiveAuction(id, Instant.now(clock))
        .orElseThrow{ AuctionNotFoundException() }

    fun findAuctionById(id: String): Auction = auctionRepository.findById(id).orElseThrow { AuctionNotFoundException() }

    fun searchAuctions(searchRequest: AuctionsSearchRequest, pageRequest: PageRequest): PagedAuctions {
        validateGeolocationFiltersIfExist(searchRequest.radius, searchRequest.cityId)
        val query: Query = buildSearchQuery(searchRequest)

        val searchedAuctions: Page<Auction> = auctionSearchRepository.search(query, pageRequest)
        val searchedAuctionsIds = searchedAuctions.toList().map { it.id }

        val auctionsViews = auctionViewsQueryFacade.getAuctionsViews(searchedAuctionsIds)
        return searchedAuctions.toPagedAuctions(auctionsViews = auctionsViews)
    }

    private fun validateGeolocationFiltersIfExist(radius: Double?, cityId: String?) {
        if (radius == null) return

        if (radius !in auctionSearchingRules.min..auctionSearchingRules.max) throw SearchRadiusOutOfBoundsException()
        if (cityId == null) throw SearchRadiusWithoutCityException()
    }

    private fun buildSearchQuery(searchRequest: AuctionsSearchRequest): Query {
        val query = Query()
        addPredicatesForActiveAuctions(query)

        searchRequest.searchPhrase?.takeIf { it.isNotBlank() }?.let {
            query.addCriteria(Criteria.where("name").regex(it, "i"))
        }
        searchRequest.categoryName?.takeIf { it.isNotBlank() }?.let {
            query.addCriteria(Criteria.where("categoryPath.pathElements.name").regex(searchRequest.categoryName, "i"))
        }
        searchRequest.categoryId?.takeIf { it.isNotBlank() }?.let {
            query.addCriteria(Criteria.where("categoryPath.pathElements.id").isEqualTo(ObjectId(searchRequest.categoryId)))
        }
        searchRequest.province?.takeIf { it.isNotBlank() }?.let {
            query.addCriteria(Criteria.where("province").regex(it, "i"))
        }
        searchRequest.cityId?.takeIf{ it.isNotBlank() }?.let { cityId ->
            val city: City = cityRepository.findById(cityId).orElseThrow { SearchRadiusWithoutCityException() }

            if (searchRequest.radius != null && searchRequest.radius > 0) {
                val point = Point(city.longitude, city.latitude)
                val distance = Distance(searchRequest.radius, Metrics.KILOMETERS)
                val circle = Circle(point, distance)
                query.addCriteria(Criteria.where("location").withinSphere(circle))
            } else {
                query.addCriteria(Criteria.where("cityId").isEqualTo(cityId))
            }
        }
        return query
    }

    private fun addPredicatesForActiveAuctions(query: Query) {
        query.addCriteria(Criteria.where("expiresAt").gte(Instant.now(clock)))
        query.addCriteria(Criteria.where("status").isEqualTo(AuctionStatus.ACCEPTED))
    }

    fun update(id: String, payload: UpdateAuctionRequest, authContext: Authentication): AuctionDetailedResponse {
        if (!validateUpdateAuctionRequest(payload)) throw InvalidAuctionUpdateRequestException()

        val auction: Auction = findAuctionById(id)
        securityHelper.assertUserIsAuthorizedForResource(authContext, auction.auctioneerId)

        val newAuctionCity: City = cityRepository.findById(payload.cityId).orElseThrow { CityNotFoundException() }

        auction.name = payload.name
        auction.price = payload.price
        auction.description = payload.description
        auction.productCondition = payload.productCondition
        auction.cityId = newAuctionCity.id
        auction.cityName = newAuctionCity.name
        auction.location = GeoJsonPoint(newAuctionCity.longitude, newAuctionCity.latitude)
        auction.province = newAuctionCity.province

        val auctionViews: Long = auctionViewsQueryFacade.getAuctionViews(auctionId = id)
        return auctionRepository.save(auction).toDetailedResponse(viewCount = auctionViews)
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
        val auctionViews: Long = auctionViewsQueryFacade.getAuctionViews(auctionId = auctionId)
        return auctionRepository.save(auction).toDetailedResponse(viewCount = auctionViews)
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
        val daysToExpire: Long = auctionCreationRules.days.toLong()
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