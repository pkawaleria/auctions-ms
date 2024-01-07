package pl.kawaleria.auctsys.auctions.domain

import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
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
import pl.kawaleria.auctsys.views.dto.AuctionsViewsRespone
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
    private val auctionValidator: AuctionValidator,
    private val clock: Clock
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun create(createRequest: CreateAuctionRequest, auctioneerId: String): AuctionDetailedResponse {
        auctionValidator.validate(payload = createRequest)
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



    private fun verifyAuctionContent(name: String?, description: String?) {
        if (!auctionVerificationRules.enabled) {
            logger.debug("Auction text properties verification is switched off and will be omitted")
            return
        }
        var isNameInappropriate = false
        var isDescriptionInappropriate = false
        when {
            description != null && name != null -> {
                val textToVerify = TextRequest("$name $description")
                val isInappropriate = contentVerificationClient.verifyText(textToVerify).isInappropriate
                isNameInappropriate = isInappropriate
                isDescriptionInappropriate = isInappropriate
            }
            name != null -> {
                val textToVerify = TextRequest("$name")
                isNameInappropriate = contentVerificationClient.verifyText(textToVerify).isInappropriate
            }
            description != null -> {
                val textToVerify = TextRequest("$description")
                isDescriptionInappropriate = contentVerificationClient.verifyText(textToVerify).isInappropriate
            }
        }
        if (isNameInappropriate || isDescriptionInappropriate) {
            logger.debug("Found explicit name or description")
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

    fun getPrivateAuctionDetails(id: String, authContext: Authentication): AuctionDetailedResponse {
        val auction: Auction = findAuctionById(id)
        securityHelper.assertUserIsAuthorizedForResource(authContext, auction.auctioneerId)
        return auction.toDetailedResponse()
    }

    private fun findActiveAuctionById(id: String): Auction = auctionRepository.findActiveAuction(id, Instant.now(clock))
        .orElseThrow{ AuctionNotFoundException() }

    fun findAuctionById(id: String): Auction = auctionRepository.findById(id).orElseThrow { AuctionNotFoundException() }

    fun searchAuctions(searchRequest: AuctionsSearchRequest, pageRequest: PageRequest): PagedAuctions {
        validateGeolocationFiltersIfExist(searchRequest.radius, searchRequest.cityId)
        val query: Query = buildSearchQuery(searchRequest)

        val searchedAuctions: Page<Auction> = auctionSearchRepository.search(query, pageRequest)
        val searchedAuctionsIds: List<String> = searchedAuctions.toList().map { it.id }

        val auctionsViews: AuctionsViewsRespone = auctionViewsQueryFacade.getAuctionsViews(searchedAuctionsIds)
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
        addPriceCriteria(searchRequest, query)
        val sortDirection: Sort.Direction = resolveSortDirection(searchRequest)
        searchRequest.sortBy?.auctionProperty.takeIf { !it.isNullOrBlank() }?.let { sortProperty ->
            query.with(Sort.by(sortDirection, sortProperty))
        }
        return query
    }

    private fun addPriceCriteria(searchRequest: AuctionsSearchRequest, query: Query
    ) {
        if (searchRequest.priceFrom != null && searchRequest.priceTo != null) {
            query.addCriteria(Criteria.where("price").gte(searchRequest.priceFrom).lte(searchRequest.priceTo))
        } else if (searchRequest.priceFrom != null) {
            query.addCriteria(Criteria.where("price").gte(searchRequest.priceFrom))
        } else if (searchRequest.priceTo != null) {
            query.addCriteria(Criteria.where("price").lte(searchRequest.priceTo))
        }
    }

    private fun resolveSortDirection(searchRequest: AuctionsSearchRequest) =
        (searchRequest.sortOrder?.let { Sort.Direction.fromString(it.toString()) }
            ?: Sort.Direction.ASC)

    private fun addPredicatesForActiveAuctions(query: Query) {
        query.addCriteria(Criteria.where("expiresAt").gte(Instant.now(clock)))
        query.addCriteria(Criteria.where("status").isEqualTo(AuctionStatus.ACCEPTED))
    }

    fun update(id: String, updateRequest: UpdateAuctionRequest, authContext: Authentication): AuctionDetailedResponse {
        auctionValidator.validate(payload = updateRequest)
        val auction: Auction = findAuctionById(id)
        securityHelper.assertUserIsAuthorizedForResource(authContext, auction.auctioneerId)

        val categoryPath: CategoryPath =
            categoryFacade.getFullCategoryPath(updateRequest.categoryId).toAuctionCategoryPathModel()

        val newAuctionCity: City = cityRepository.findById(updateRequest.cityId).orElseThrow { CityNotFoundException() }

        if (isVerificationApplicable(auction, updateRequest)) {
            verifyAuctionContent(updateRequest.name, updateRequest.description)
        }

        auction.name = updateRequest.name
        auction.price = updateRequest.price
        auction.description = updateRequest.description
        auction.productCondition = updateRequest.productCondition
        auction.cityId = newAuctionCity.id
        auction.cityName = newAuctionCity.name
        auction.location = GeoJsonPoint(newAuctionCity.longitude, newAuctionCity.latitude)
        auction.province = newAuctionCity.province
        auction.category = categoryPath.lastCategory()
        auction.categoryPath = categoryPath
        auction.phoneNumber = updateRequest.phoneNumber

        val auctionViews: Long = auctionViewsQueryFacade.getAuctionViews(auctionId = id)
        return auctionRepository.save(auction).toDetailedResponse(viewCount = auctionViews)
    }


    private fun isVerificationApplicable(
        auction: Auction,
        updateRequest: UpdateAuctionRequest
    ) = auction.name != updateRequest.name || auction.description !== updateRequest.description


    fun delete(auctionId: String, authContext: Authentication) {
        val auctionToDelete: Auction = findAuctionById(auctionId)
        securityHelper.assertUserIsAuthorizedForResource(authContext, auctionToDelete.auctioneerId)
        archive(auctionId, authContext)
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

    fun getAuctionsByIds(ids: List<String>): List<AuctionSimplifiedResponse> {
        return auctionRepository.findByIdIn(ids).map { it.toSimplifiedResponse() }
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

fun CategoryPathResponse.toAuctionCategoryPathModel(): CategoryPath {
    return CategoryPath(this.path.map { it.toAuctionCategoryModel() }.toMutableList())
}

private fun CategoryNameResponse.toAuctionCategoryModel(): Category {
    return Category(this.id, this.name)
}