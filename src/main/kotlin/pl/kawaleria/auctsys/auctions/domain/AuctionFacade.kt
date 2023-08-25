package pl.kawaleria.auctsys.auctions.domain

import org.springframework.data.domain.PageRequest
import pl.kawaleria.auctsys.auctions.dto.exceptions.AuctionNotFoundException
import pl.kawaleria.auctsys.auctions.dto.requests.AuctionsSearchRequest
import pl.kawaleria.auctsys.auctions.dto.requests.CreateAuctionRequest
import pl.kawaleria.auctsys.auctions.dto.requests.UpdateAuctionRequest
import pl.kawaleria.auctsys.auctions.dto.responses.*
import pl.kawaleria.auctsys.categories.domain.CategoryFacade
import pl.kawaleria.auctsys.categories.dto.response.CategoryNameResponse
import pl.kawaleria.auctsys.categories.dto.response.CategoryPathResponse
import java.time.Clock
import java.time.Duration
import java.time.Instant

class AuctionFacade(private val auctionRepository: AuctionRepository,
                    private val categoryFacade: CategoryFacade,
                    private val auctionRules: AuctionRules,
                    private val auctionCategoryDeleter: AuctionCategoryDeleter,
                    private val clock: Clock) {
    fun findAuctionsByAuctioneer(auctioneerId: String): MutableList<Auction> = auctionRepository.findAuctionsByAuctioneerId(auctioneerId)

    fun findAuctionById(id: String): Auction = auctionRepository.findById(id).orElseThrow { AuctionNotFoundException() }

    fun changeCategory(auctionId: String, categoryId: String) {
        val auction = findAuctionById(auctionId)
        val pathDto = categoryFacade.getFullCategoryPath(categoryId)
        val path = pathDto.toAuctionCategoryPathModel()
        auction.assignPath(path)
        auctionRepository.save(auction)
    }

    fun addNewAuction(createRequest: CreateAuctionRequest, auctioneerId: String): Auction {
        if (!validateCreateAuctionRequest(createRequest)) {
            throw ApiException(400, "CreateAuctionRequest is not valid")
        }
        val categoryPath = categoryFacade.getFullCategoryPath(createRequest.categoryId).toAuctionCategoryPathModel()

        val auction = Auction(
                name = createRequest.name,
                description = createRequest.description,
                price = createRequest.price,
                auctioneerId = auctioneerId,
                thumbnail = byteArrayOf(),
                expiresAt = newExpirationInstant(),
                category = categoryPath.lastCategory(),
                categoryPath = categoryPath,
        )
        return auctionRepository.save(auction)
    }

    fun update(id: String, payload: UpdateAuctionRequest): Auction {
        if (validateUpdateAuctionRequest(payload)) {
            val auction = findAuctionById(id)

            auction.name = payload.name
            auction.price = payload.price
            auction.description = payload.description

            return auctionRepository.save(auction)
        } else throw ApiException(400, "UpdateAuctionRequest is not valid")
    }

    fun delete(auctionId: String): Unit = auctionRepository.delete(findAuctionById(auctionId))

    fun eraseCategoryFromAuctions(categoryName: String) {
        auctionCategoryDeleter.eraseCategoryFromAuctions(categoryName)
    }

    fun saveThumbnail(auctionId: String, byteArray: ByteArray) {
        val auction = findAuctionById(auctionId)
        auction.thumbnail = byteArray
        auctionRepository.save(auction)
    }

    fun searchAuctions(searchRequest: AuctionsSearchRequest, pageRequest: PageRequest): PagedAuctions {
        val categoryName: String? = searchRequest.categoryName
        val searchPhrase: String? = searchRequest.searchPhrase?.takeIf { it.isNotBlank() }

        return when {
            searchPhrase != null && categoryName != null ->
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

    fun archive(auctionId: String) {
        val auction: Auction = findAuctionById(auctionId)
        auction.archive()
        auctionRepository.save(auction)
    }

    fun reject(auctionId: String) {
        val auction: Auction = findAuctionById(auctionId)
        auction.reject()
        auctionRepository.save(auction)
    }

    private fun newExpirationInstant(): Instant {
        val daysToExpire = auctionRules.days.toLong()
        return Instant.now(clock).plusSeconds(Duration.ofDays(daysToExpire).toSeconds())
    }

    private fun validateCreateAuctionRequest(payload: CreateAuctionRequest): Boolean {
        val validatedName = validateName(payload.name)
        val validatedDescription = validateDescription(payload.description)
        val validatedPrice = validatePrice(payload.price)

        return (validatedName && validatedDescription && validatedPrice)
    }

    private fun validateUpdateAuctionRequest(payload: UpdateAuctionRequest): Boolean {
        val validatedName = validateName(payload.name)
        val validatedDescription = validateDescription(payload.description)
        val validatedPrice = validatePrice(payload.price)

        return (validatedName && validatedDescription && validatedPrice)
    }

    private fun validateName(name: String): Boolean {
        val regex = "^[a-zA-Z0-9 .]*$".toRegex()

        return name.isNotEmpty() && name.length in 5..100 && regex.matches(name)
    }

    private fun validateDescription(description: String): Boolean {
        val regex = "^[a-zA-Z0-9 .]*$".toRegex()

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