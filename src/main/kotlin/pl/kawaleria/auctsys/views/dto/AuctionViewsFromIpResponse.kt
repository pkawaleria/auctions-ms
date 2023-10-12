package pl.kawaleria.auctsys.views.dto

import java.time.Instant

data class AuctionViewsFromIpResponse(
    val auctionId: String,
    val ipAddress: String,
    val viewsTimestamps: List<Instant> = listOf(),
    val viewCounter: Int = 0
)
