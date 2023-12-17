package pl.kawaleria.auctsys.auctions.dto.events

import java.time.Instant

data class InappropriateAuctionContentDetectedEvent(
    val auctionId: String,
    val ipAddress: String,
    val auctioneerId: String,
    val timestamp: Instant,
    val violentText: String,
)
