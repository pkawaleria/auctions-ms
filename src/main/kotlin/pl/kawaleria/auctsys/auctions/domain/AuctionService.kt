package pl.kawaleria.auctsys.auctions.domain

import org.springframework.data.domain.PageRequest
import pl.kawaleria.auctsys.auctions.dto.requests.AuctionsSearchRequest
import pl.kawaleria.auctsys.auctions.dto.requests.CreateAuctionRequest
import pl.kawaleria.auctsys.auctions.dto.requests.UpdateAuctionRequest
import pl.kawaleria.auctsys.auctions.dto.responses.ApiException
import pl.kawaleria.auctsys.auctions.dto.responses.PagedAuctions
import pl.kawaleria.auctsys.auctions.dto.responses.toPagedAuctions

class AuctionService(private val auctionRepository: AuctionRepository) {

    fun findAuctionsByAuctioneerId(auctioneerId: String): MutableList<Auction> = auctionRepository.findAuctionsByAuctioneerId(auctioneerId)

    fun findAuctionByIdAndAuctioneerId(id: String, auctioneerId: String): Auction = auctionRepository.findAuctionByIdAndAuctioneerId(id, auctioneerId)

    fun addNewAuction(payload: CreateAuctionRequest, auctioneerId: String): Auction {
        if (validateCreateAuctionRequest(payload)) {
            val auction = Auction(
                name = payload.name,
                category = payload.category,
                description = payload.description,
                price = payload.price,
                auctioneerId = auctioneerId
            )

            return auctionRepository.save(auction)
        } else throw ApiException(400, "CreateAuctionRequest is not valid")
    }

    fun updateAndSaveAuction(id: String, auctioneerId: String, payload: UpdateAuctionRequest): Auction {
        if (validateUpdateAuctionRequest(payload)) {
            val auction = findAuctionByIdAndAuctioneerId(id, auctioneerId)

            auction.name = payload.name
            auction.price = payload.price
            auction.category = payload.category
            auction.description = payload.description

            return auctionRepository.save(auction)
        } else throw ApiException(400, "UpdateAuctionRequest is not valid")
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

        return description.isNotEmpty() && description.length in 20 .. 500 && regex.matches(description)
    }

    private fun validatePrice(price: Double): Boolean = price > 0

    fun delete(userId: String, auctionId: String) {
        val auction = findAuctionByIdAndAuctioneerId(userId, auctionId)

        auctionRepository.delete(auction)
    }

    fun searchAuctions(searchRequest: AuctionsSearchRequest, pageRequest: PageRequest): PagedAuctions {
        val mappedCategory: Category? = searchRequest.category?.let { mapToCategory(it) }
        val searchPhrase: String? = searchRequest.searchPhrase?.takeIf { it.isNotBlank() }

        return when {
            searchPhrase != null && mappedCategory != null ->
                auctionRepository.findByNameContainingIgnoreCaseAndCategoryEquals(searchPhrase, mappedCategory, pageRequest)

            searchPhrase == null && mappedCategory != null ->
                auctionRepository.findByCategoryEquals(mappedCategory, pageRequest)

            searchPhrase != null && mappedCategory == null ->
                auctionRepository.findByNameContainingIgnoreCase(searchPhrase, pageRequest)

            else -> auctionRepository.findAll(pageRequest)
        }.toPagedAuctions()
    }

    private fun mapToCategory(categoryString: String): Category? {
        val validCategories = enumValues<Category>().map { it.name }
        if (categoryString in validCategories) {
            return Category.valueOf(categoryString)
        }
        return null
    }
}