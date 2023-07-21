package pl.kawaleria.auctsys.controllers

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.kawaleria.auctsys.models.Auction
import pl.kawaleria.auctsys.requests.AuctionsSearchRequest
import pl.kawaleria.auctsys.requests.CreateAuctionDto
import pl.kawaleria.auctsys.requests.UpdateAuctionDto
import pl.kawaleria.auctsys.responses.ApiException
import pl.kawaleria.auctsys.responses.AuctionDetailedResponse
import pl.kawaleria.auctsys.responses.PagedAuctions
import pl.kawaleria.auctsys.responses.toDto
import pl.kawaleria.auctsys.services.AuctionService

@RestController
@RequestMapping("/auction-service")
class AuctionController(private val auctionService: AuctionService) {

    @GetMapping("/auctions")
    fun searchAuctions(
            @RequestParam(required = false, defaultValue = "0") page: Int,
            @RequestParam(required = false, defaultValue = "10") pageSize: Int,
            @RequestParam(required = false) searchPhrase: String?,
            @RequestParam(required = false) category: String?
    ): PagedAuctions {

        val pageRequest = PageRequest.of(page, pageSize)
        val searchRequest = AuctionsSearchRequest(searchPhrase, category)

        return auctionService.searchAuctions(searchRequest, pageRequest)
    }
    @GetMapping("/users/{userId}/auctions/{auctionId}")
    fun getAuction(
        @PathVariable userId: String,
        @PathVariable auctionId: String): AuctionDetailedResponse {
        return auctionService.findAuctionByIdAndAuctioneerId(userId, auctionId)?.toDto() ?: throw ApiException(404, "Auction not found")
    }

    @GetMapping("/users/{userId}/auctions")
    fun getAuctions(@PathVariable userId: String): List<AuctionDetailedResponse> {
        return auctionService.findAuctionsByAuctioneerId(userId).map { auction -> auction.toDto() }
    }

    @PostMapping("/users/{userId}/auctions")
    fun addAuction(
        @PathVariable userId: String,
        @RequestBody payload: CreateAuctionDto
    ): AuctionDetailedResponse {
        val auction = Auction(
            name = payload.name,
            category = payload.category,
            description = payload.description,
            price = payload.price,
            auctioneerId = userId
        )

        auctionService.save(auction)
        return auction.toDto()
    }

    @PutMapping("/users/{userId}/auctions/{auctionId}")
    fun updateAuction(
        @PathVariable userId: String,
        @PathVariable auctionId: String,
        @RequestBody payload: UpdateAuctionDto
    ): AuctionDetailedResponse {
        val auction = auctionService.findAuctionByIdAndAuctioneerId(auctionId, userId) ?: throw ApiException(404, "Auction not found")

        auction.name = payload.name
        auction.price = payload.price
        auction.category = payload.category
        auction.description = payload.description

        auctionService.save(auction)
        return auction.toDto()
    }

    @DeleteMapping("/users/{userId}/auctions/{auctionId}")
    fun deleteAuction(
        @PathVariable userId: String,
        @PathVariable auctionId: String): ResponseEntity<String> {
        val auction = auctionService.findAuctionByIdAndAuctioneerId(userId, auctionId) ?: throw ApiException(404, "Auction not found")

        auctionService.delete(auction)

        return ResponseEntity.noContent().build()
    }
}