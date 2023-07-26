package pl.kawaleria.auctsys.auctions

import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.kawaleria.auctsys.auctions.dto.requests.AuctionsSearchRequest
import pl.kawaleria.auctsys.auctions.dto.requests.CreateAuctionRequest
import pl.kawaleria.auctsys.auctions.dto.requests.UpdateAuctionRequest
import pl.kawaleria.auctsys.auctions.domain.AuctionService
import pl.kawaleria.auctsys.auctions.dto.responses.*

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
        @PathVariable auctionId: String
    ): AuctionSimplifiedResponse {
        return auctionService.findAuctionByIdAndAuctioneerId(userId, auctionId).toSimplifiedResponse()
    }

    @GetMapping("/users/{userId}/auctions")
    fun getAuctions(@PathVariable userId: String): List<AuctionSimplifiedResponse> {
        return auctionService.findAuctionsByAuctioneerId(userId).map { auction -> auction.toSimplifiedResponse() }
    }

    @PostMapping("/users/{userId}/auctions")
    fun addAuction(
        @PathVariable userId: String,
        @RequestBody payload: CreateAuctionRequest
    ): AuctionSimplifiedResponse {
        return auctionService.addNewAuction(payload, userId).toSimplifiedResponse()
    }

    @PutMapping("/users/{userId}/auctions/{auctionId}")
    fun updateAuction(
        @PathVariable userId: String,
        @PathVariable auctionId: String,
        @RequestBody payload: UpdateAuctionRequest
    ): AuctionDetailedResponse {
        return auctionService.updateAndSaveAuction(auctionId, userId, payload).toDto()
    }

    @DeleteMapping("/users/{userId}/auctions/{auctionId}")
    fun deleteAuction(
        @PathVariable userId: String,
        @PathVariable auctionId: String)
    : ResponseEntity<String> {
        auctionService.delete(userId, auctionId)

        return ResponseEntity.noContent().build()
    }
}