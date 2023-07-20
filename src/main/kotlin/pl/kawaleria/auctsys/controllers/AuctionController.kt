package pl.kawaleria.auctsys.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.kawaleria.auctsys.models.Auction
import pl.kawaleria.auctsys.requests.CreateAuctionDto
import pl.kawaleria.auctsys.requests.UpdateAuctionDto
import pl.kawaleria.auctsys.responses.ApiException
import pl.kawaleria.auctsys.responses.AuctionDto
import pl.kawaleria.auctsys.responses.toDto
import pl.kawaleria.auctsys.services.AuctionService

@RestController
@RequestMapping("/auction-service/users")
class AuctionController(private val auctionService: AuctionService) {

    @GetMapping("/{userId}/auctions/{auctionId}")
    fun getAuction(
        @PathVariable userId: String,
        @PathVariable auctionId: String): AuctionDto {
        return auctionService.findAuctionByIdAndAuctioneerId(userId, auctionId)?.toDto() ?: throw ApiException(404, "Auction not found")
    }

    @GetMapping("/{userId}/auctions")
    fun getAuctions(@PathVariable userId: String): List<AuctionDto> {
        return auctionService.findAuctionsByAuctioneerId(userId).map { auction -> auction.toDto() }
    }

    @PostMapping("/{userId}/auctions")
    fun addAuction(
        @PathVariable userId: String,
        @RequestBody payload: CreateAuctionDto
    ): AuctionDto {
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

    @PutMapping("/{userId}/auctions/{auctionId}")
    fun updateAuction(
        @PathVariable userId: String,
        @PathVariable auctionId: String,
        @RequestBody payload: UpdateAuctionDto
    ): AuctionDto {
        val auction = auctionService.findAuctionByIdAndAuctioneerId(auctionId, userId) ?: throw ApiException(404, "Auction not found")

        auction.name = payload.name
        auction.price = payload.price
        auction.category = payload.category
        auction.description = payload.description

        auctionService.save(auction)
        return auction.toDto()
    }

    @DeleteMapping("/{userId}/auctions/{auctionId}")
    fun deleteAuction(
        @PathVariable userId: String,
        @PathVariable auctionId: String): ResponseEntity<String> {
        val auction = auctionService.findAuctionByIdAndAuctioneerId(userId, auctionId) ?: throw ApiException(404, "Auction not found")

        auctionService.delete(auction)

        return ResponseEntity.noContent().build()
    }
}