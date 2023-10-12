package pl.kawaleria.auctsys.auctions.dto.events

data class AuctionViewedEvent(
    val ipAddress : String,
    val auctionId: String,
    val auctioneerId: String? = null
)