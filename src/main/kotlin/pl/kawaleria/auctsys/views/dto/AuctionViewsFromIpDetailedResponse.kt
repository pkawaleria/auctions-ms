package pl.kawaleria.auctsys.views.dto

import pl.kawaleria.auctsys.auctions.dto.responses.AuctionSimplifiedResponse

data class AuctionViewsFromIpDetailedResponse(
    val viewsDetails: AuctionViewsFromIpResponse,
    val auctionDetails: AuctionSimplifiedResponse
)
