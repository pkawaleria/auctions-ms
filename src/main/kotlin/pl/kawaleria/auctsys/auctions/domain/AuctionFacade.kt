package pl.kawaleria.auctsys.auctions.domain

import org.springframework.data.domain.PageRequest
import pl.kawaleria.auctsys.auctions.dto.exceptions.AuctionNotFoundException
import pl.kawaleria.auctsys.auctions.dto.requests.AuctionsSearchRequest
import pl.kawaleria.auctsys.auctions.dto.requests.CreateAuctionRequest
import pl.kawaleria.auctsys.auctions.dto.requests.UpdateAuctionRequest
import pl.kawaleria.auctsys.auctions.dto.responses.ApiException
import pl.kawaleria.auctsys.auctions.dto.responses.PagedAuctions
import pl.kawaleria.auctsys.auctions.dto.responses.toPagedAuctions
import java.time.Clock
import java.time.Duration
import java.time.Instant

class AuctionFacade(private val auctionRepository: AuctionRepository,
                    private val auctionRules: AuctionRules,
                    private val clock: Clock) {
    fun findAuctionsByAuctioneer(auctioneerId: String): MutableList<Auction> = auctionRepository.findAuctionsByAuctioneerId(auctioneerId)

    fun findAuctionById(id: String): Auction = auctionRepository.findById(id).orElseThrow { ApiException(404, "Auction does not exists") }

    fun addNewAuction(payload: CreateAuctionRequest, auctioneerId: String): Auction {
        if (validateCreateAuctionRequest(payload)) {
            val auction = Auction(
                    name = payload.name,
                    category = payload.category,
                    description = payload.description,
                    price = payload.price,
                    auctioneerId = auctioneerId,
                    expiresAt = newExpirationInstant()
            )

            return auctionRepository.save(auction)
        } else throw ApiException(400, "CreateAuctionRequest is not valid")
    }

    fun update(id: String, payload: UpdateAuctionRequest): Auction {
        if (validateUpdateAuctionRequest(payload)) {
            val auction = findAuctionById(id)

            auction.name = payload.name
            auction.price = payload.price
            auction.category = payload.category
            auction.description = payload.description

            return auctionRepository.save(auction)
        } else throw ApiException(400, "UpdateAuctionRequest is not valid")
    }

    fun delete(auctionId: String): Unit = auctionRepository.deleteById(auctionId)

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

    fun find(auctionId: String): Auction {
        return auctionRepository.findById(auctionId)
                .orElseThrow { AuctionNotFoundException() }
    }

    fun accept(auctionId: String) {
        val auction: Auction = find(auctionId)
        auction.accept()
        auctionRepository.save(auction)
    }

    fun archive(auctionId: String) {
        val auction: Auction = find(auctionId)
        auction.archive()
        auctionRepository.save(auction)
    }

    fun reject(auctionId: String) {
        val auction: Auction = find(auctionId)
        auction.reject()
        auctionRepository.save(auction)
    }

    private fun newExpirationInstant(): Instant {
        val daysToExpire = auctionRules.days.toLong()
        return Instant.now(clock).plusSeconds(Duration.ofDays(daysToExpire).toSeconds())
    }
}