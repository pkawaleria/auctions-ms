package pl.kawaleria.auctsys.auctions

import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.kawaleria.auctsys.auctions.domain.AuctionFacade
import pl.kawaleria.auctsys.auctions.dto.requests.AuctionsSearchRequest
import pl.kawaleria.auctsys.auctions.dto.requests.CreateAuctionRequest
import pl.kawaleria.auctsys.auctions.dto.requests.UpdateAuctionRequest
import pl.kawaleria.auctsys.auctions.dto.responses.*

@RestController
@RequestMapping("/auction-service")
class AuctionCrudController(private val auctionFacade: AuctionFacade) {

    @GetMapping("/auctions")
    fun searchAuctions(
            @RequestParam(required = false, defaultValue = "0") page: Int,
            @RequestParam(required = false, defaultValue = "10") pageSize: Int,
            @RequestParam(required = false) searchPhrase: String?,
            @RequestParam(required = false) category: String?
    ): PagedAuctions {
        val pageRequest: PageRequest = PageRequest.of(page, pageSize)
        val searchRequest = AuctionsSearchRequest(searchPhrase, category)

        return auctionFacade.searchAuctions(searchRequest, pageRequest)
    }

    @GetMapping("/auctions/{auctionId}")
    fun getAuction(
            @PathVariable auctionId: String
    ): AuctionDetailedResponse {
        return auctionFacade.findAuctionById(auctionId).toDetailedResponse()
    }

    @PutMapping("/auctions/{auctionId}/categories/{categoryId}")
    fun changeCategory(
            @PathVariable auctionId: String,
            @PathVariable categoryId: String)
            : AuctionDetailedResponse {
        return auctionFacade.changeCategory(auctionId, categoryId)
    }

    @GetMapping("/users/{userId}/auctions")
    fun getAuctions(@PathVariable userId: String): List<AuctionSimplifiedResponse> {
        return auctionFacade.findAuctionsByAuctioneer(userId).map { auction -> auction.toSimplifiedResponse() }
    }

    @PostMapping("/users/{userId}/auctions")
    fun addAuction(
            @PathVariable userId: String,
            @RequestBody payload: CreateAuctionRequest
    ): AuctionDetailedResponse {
        return auctionFacade.addNewAuction(payload, userId).toDetailedResponse()
    }

    @PutMapping("/users/{userId}/auctions/{auctionId}")
    fun updateAuction(
            @PathVariable userId: String,
            @PathVariable auctionId: String,
            @RequestBody payload: UpdateAuctionRequest
    ): AuctionDetailedResponse {
        return auctionFacade.update(auctionId, payload).toDetailedResponse()
    }

    @DeleteMapping("/users/{userId}/auctions/{auctionId}")
    fun deleteAuction(
            @PathVariable userId: String,
            @PathVariable auctionId: String)
            : ResponseEntity<Unit> {
        auctionFacade.delete(auctionId)
        return ResponseEntity.noContent().build()
    }
}