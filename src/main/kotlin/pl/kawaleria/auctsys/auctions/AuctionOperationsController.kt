package pl.kawaleria.auctsys.auctions

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import pl.kawaleria.auctsys.auctions.domain.AuctionFacade

@RestController
@RequestMapping("/auction-service")
class AuctionOperationsController(private val auctionFacade: AuctionFacade) {

    @PostMapping("/auctions/{auctionId}/operations/accept")
    fun accept(@PathVariable auctionId: String): ResponseEntity<Unit> {
        auctionFacade.accept(auctionId)
        return ResponseEntity.accepted().build()
    }

    @PostMapping("/auctions/{auctionId}/operations/archive")
    fun archive(@PathVariable auctionId: String): ResponseEntity<Unit> {
        auctionFacade.archive(auctionId)
        return ResponseEntity.accepted().build()
    }

    @PostMapping("/auctions/{auctionId}/operations/reject")
    fun reject(@PathVariable auctionId: String): ResponseEntity<Unit> {
        auctionFacade.reject(auctionId)
        return ResponseEntity.accepted().build()
    }
}