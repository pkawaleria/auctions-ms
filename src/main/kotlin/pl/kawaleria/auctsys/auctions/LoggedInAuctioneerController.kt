package pl.kawaleria.auctsys.auctions

import org.springframework.data.domain.PageRequest
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.CurrentSecurityContext
import org.springframework.web.bind.annotation.*
import pl.kawaleria.auctsys.auctions.domain.AuctionFacade
import pl.kawaleria.auctsys.auctions.dto.responses.*
import pl.kawaleria.auctsys.configs.toAuctioneerId

@RestController
@RequestMapping("/auction-service/active-auctioneer")
class LoggedInAuctioneerController(private val auctionFacade: AuctionFacade) {

    @GetMapping("/rejected-auctions")
    fun getRejectedAuctions(
            @RequestParam(required = false, defaultValue = "0") page: Int,
            @RequestParam(required = false, defaultValue = "10") pageSize: Int,
            @CurrentSecurityContext(expression = "authentication") authContext: Authentication): PagedAuctions {
        return auctionFacade.findRejectedAuctions(authContext.toAuctioneerId(), PageRequest.of(page, pageSize))
    }

    @GetMapping("/active-auctions")
    fun getActiveAuctions(
            @RequestParam(required = false, defaultValue = "0") page: Int,
            @RequestParam(required = false, defaultValue = "10") pageSize: Int,
            @CurrentSecurityContext(expression = "authentication") authContext: Authentication): PagedAuctions {
        return auctionFacade.findActiveAuctions(authContext.toAuctioneerId(), PageRequest.of(page, pageSize))
    }

    @GetMapping("/expired-auctions")
    fun getExpiredAuctions(
            @RequestParam(required = false, defaultValue = "0") page: Int,
            @RequestParam(required = false, defaultValue = "10") pageSize: Int,
            @CurrentSecurityContext(expression = "authentication") authContext: Authentication): PagedAuctions {
        return auctionFacade.findExpiredAuctions(authContext.toAuctioneerId(), PageRequest.of(page, pageSize))
    }

    @GetMapping("/archived-auctions")
    fun getArchivedAuctions(
            @RequestParam(required = false, defaultValue = "0") page: Int,
            @RequestParam(required = false, defaultValue = "10") pageSize: Int,
            @CurrentSecurityContext(expression = "authentication") authContext: Authentication): PagedAuctions {
        return auctionFacade.findArchivedAuctions(authContext.toAuctioneerId(), PageRequest.of(page, pageSize))
    }

    @GetMapping("/awaiting-auctions")
    fun getAwaitingAuctions(
            @RequestParam(required = false, defaultValue = "0") page: Int,
            @RequestParam(required = false, defaultValue = "10") pageSize: Int,
            @CurrentSecurityContext(expression = "authentication") authContext: Authentication): PagedAuctions {
        return auctionFacade.findAwaitingAcceptanceAuctions(authContext.toAuctioneerId(), PageRequest.of(page, pageSize))
    }


}