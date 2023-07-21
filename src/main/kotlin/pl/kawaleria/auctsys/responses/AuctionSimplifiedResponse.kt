package pl.kawaleria.auctsys.responses

import pl.kawaleria.auctsys.models.Auction
import pl.kawaleria.auctsys.models.Category

data class AuctionSimplifiedResponse(
    val id: String?,
    val name: String?,
    val category: Category?,
    val price: Double?
)

fun Auction.toSimplifiedResponse(): AuctionSimplifiedResponse = AuctionSimplifiedResponse(id, name, category, price)
