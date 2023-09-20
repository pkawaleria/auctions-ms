package pl.kawaleria.auctsys.auctions

import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.web.bind.annotation.*
import pl.kawaleria.auctsys.auctions.domain.AuctionFacade
import pl.kawaleria.auctsys.auctions.dto.requests.AuctionsSearchRequest
import pl.kawaleria.auctsys.auctions.dto.requests.CreateAuctionRequest
import pl.kawaleria.auctsys.auctions.dto.requests.UpdateAuctionRequest
import pl.kawaleria.auctsys.auctions.dto.responses.*
import pl.kawaleria.auctsys.configs.toAuctioneerId

@RestController
@RequestMapping("/auction-service")
class AuctionCrudController(private val auctionFacade: AuctionFacade) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

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
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    fun changeCategory(
            @PathVariable auctionId: String,
            @PathVariable categoryId: String,
            @CurrentSecurityContext(expression = "authentication") authContext: Authentication)
            : AuctionDetailedResponse {
        return auctionFacade.changeCategory(auctionId, categoryId, authContext)
    }

    @GetMapping("/users/{userId}/auctions")
    fun getAuctions(@PathVariable userId: String): List<AuctionSimplifiedResponse> {
        return auctionFacade.findAuctionsByAuctioneer(userId).map { auction -> auction.toSimplifiedResponse() }
    }

    @PostMapping("/auctions")
    @PreAuthorize("hasRole('ROLE_USER')")
    fun addAuction(
            @RequestBody payload: CreateAuctionRequest,
            @CurrentSecurityContext(expression = "authentication") authContext: Authentication,
    ): AuctionDetailedResponse {
        logger.info("Creating auction of request {$payload} for logged in auctioneer of id {${authContext.toAuctioneerId()}}")
        return auctionFacade.create(payload, authContext.toAuctioneerId()).toDetailedResponse()
    }

    @PutMapping("/auctions/{auctionId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    fun updateAuction(
            @PathVariable auctionId: String,
            @RequestBody payload: UpdateAuctionRequest,
            @CurrentSecurityContext(expression = "authentication") authContext: Authentication
    ): AuctionDetailedResponse {
        return auctionFacade.update(auctionId, payload, authContext).toDetailedResponse()
    }

    @DeleteMapping("/auctions/{auctionId}")
    @PreAuthorize("hasRole('ROLE_USER') or hasRole('ROLE_ADMIN')")
    fun deleteAuction(
            @PathVariable auctionId: String,
            @CurrentSecurityContext(expression = "authentication") authContext: Authentication)
            : ResponseEntity<Unit> {
        auctionFacade.delete(auctionId, authContext)
        return ResponseEntity.noContent().build()
    }
}