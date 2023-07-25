package pl.kawaleria.auctsys.auctions.domain

import org.springframework.data.domain.PageRequest
import pl.kawaleria.auctsys.auctions.dto.requests.AuctionsSearchRequest
import pl.kawaleria.auctsys.auctions.dto.responses.PagedAuctions
import pl.kawaleria.auctsys.auctions.dto.responses.toPagedAuctions

class AuctionService(private val auctionRepository: AuctionRepository) {
    fun findAuctionsByAuctioneerId(auctioneerId: String): MutableList<Auction> = auctionRepository.findAuctionsByAuctioneerId(auctioneerId)
    fun findAuctionByIdAndAuctioneerId(id: String, auctioneerId: String): Auction? = auctionRepository.findAuctionByIdAndAuctioneerId(id, auctioneerId)
    fun save(auction: Auction): Auction = auctionRepository.save(auction)
    fun delete(auction: Auction): Unit = auctionRepository.delete(auction)
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