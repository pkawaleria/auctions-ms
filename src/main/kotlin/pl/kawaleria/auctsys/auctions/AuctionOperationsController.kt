package pl.kawaleria.auctsys.auctions

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.web.bind.annotation.*
import pl.kawaleria.auctsys.auctions.domain.AuctionFacade

@RestController
@RequestMapping("/auction-service/auctions")
class AuctionOperationsController(private val auctionFacade: AuctionFacade) {

    @PostMapping("/{auctionId}/operations/accept")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun accept(@PathVariable auctionId: String): ResponseEntity<Unit> {
        auctionFacade.accept(auctionId)
        return ResponseEntity.accepted().build()
    }

    @PostMapping("/{auctionId}/operations/archive")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    fun archive(@PathVariable auctionId: String,
                @CurrentSecurityContext(expression = "authentication") authContext: Authentication): ResponseEntity<Unit> {
        auctionFacade.archive(auctionId, authContext)
        return ResponseEntity.accepted().build()
    }

    @PostMapping("/{auctionId}/operations/reject")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun reject(@PathVariable auctionId: String): ResponseEntity<Unit> {
        auctionFacade.reject(auctionId)
        return ResponseEntity.accepted().build()
    }
}