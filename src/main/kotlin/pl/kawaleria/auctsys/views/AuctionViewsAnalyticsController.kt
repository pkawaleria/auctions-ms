package pl.kawaleria.auctsys.views

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.*
import pl.kawaleria.auctsys.auctions.domain.AuctionFacade
import pl.kawaleria.auctsys.auctions.dto.responses.*
import pl.kawaleria.auctsys.commons.IpAddressResolver
import pl.kawaleria.auctsys.views.domain.AuctionViewsQueryFacade
import pl.kawaleria.auctsys.views.dto.AuctionViewsFromIpDetailedResponse

@RestController
@RequestMapping("/auction-service/viewed-auctions")
class AuctionViewsAnalyticsController(
    private val auctionFacade: AuctionFacade,
    private val viewsFacade: AuctionViewsQueryFacade,
    private val ipAddressResolver: IpAddressResolver,
) {

    @GetMapping("/most-views")
    fun getMostlyViewedAuctionsFromIp(
        request: HttpServletRequest,
        @RequestParam(required = false, defaultValue = "12") numberOfElements: Int
    ): List<AuctionViewsFromIpDetailedResponse> {
        val ipAddress: String = ipAddressResolver.getIpAddress(request)

        val mostViewedAuctions = viewsFacade.getMostViewedAuctionsFromIpAddress(ipAddress)
        val ids = mostViewedAuctions.map { it.auctionId }
        val auctions = auctionFacade.getAuctionsByIds(ids)

        return mostViewedAuctions.mapNotNull { viewDetails ->
            auctions.find { it.id == viewDetails.auctionId }?.let {
                AuctionViewsFromIpDetailedResponse(auctionDetails = it, viewsDetails = viewDetails)
            }
        }
    }

    @GetMapping("/latest-views")
    fun getLatelyViewedAuctionsFromIp(
        @RequestParam(required = false, defaultValue = "12") numberOfElements: Int,
        request: HttpServletRequest
    ): List<AuctionViewsFromIpDetailedResponse> {
        val ipAddress: String = ipAddressResolver.getIpAddress(request)

        val recentViewsFromIpAddress = viewsFacade.getRecentViewsFromIpAddress(ipAddress, numberOfElements)
        val ids = recentViewsFromIpAddress.map { it.auctionId }
        val auctions = auctionFacade.getAuctionsByIds(ids)

        return recentViewsFromIpAddress.mapNotNull { viewDetails ->
            auctions.find { it.id == viewDetails.auctionId }?.let {
                AuctionViewsFromIpDetailedResponse(auctionDetails = it, viewsDetails = viewDetails)
            }
        }
    }

}