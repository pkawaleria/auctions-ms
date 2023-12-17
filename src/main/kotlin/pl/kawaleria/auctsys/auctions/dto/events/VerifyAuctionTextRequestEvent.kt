package pl.kawaleria.auctsys.auctions.dto.events

import java.time.Instant

data class VerifyAuctionTextRequestEvent(
    val auctionId: String,
    val title: String,
    val description: String,
    val timestamp: Instant = Instant.now(),
)
