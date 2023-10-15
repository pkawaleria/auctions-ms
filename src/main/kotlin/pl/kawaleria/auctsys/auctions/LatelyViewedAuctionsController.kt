package pl.kawaleria.auctsys.auctions

import org.springframework.web.bind.annotation.*
import pl.kawaleria.auctsys.auctions.domain.AuctionFacade
import pl.kawaleria.auctsys.auctions.dto.responses.*

@RestController
@RequestMapping("/auction-service/viewed-auctions")
class LatelyViewedAuctionsController(private val auctionFacade: AuctionFacade) {

//    @GetMapping("/auctions")
//    fun getAll(
//        @RequestParam(required = false, defaultValue = "0") page: Int,
//        @RequestParam(required = false, defaultValue = "10") pageSize: Int,
//        @CurrentSecurityContext(expression = "authentication") authContext: Authentication
//    ): PagedAuctions {
//        return auctionFacade.findAuctionsByAuctioneer(authContext.toAuctioneerId(), PageRequest.of(page, pageSize))
//    }


}