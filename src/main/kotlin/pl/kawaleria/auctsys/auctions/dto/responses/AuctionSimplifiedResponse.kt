package pl.kawaleria.auctsys.auctions.dto.responses

import pl.kawaleria.auctsys.auctions.domain.Auction
import pl.kawaleria.auctsys.auctions.domain.Category

data class AuctionSimplifiedResponse(
        val id: String?,
        val name: String?,
        val category: Category?,
        val price: Double?
)

fun Auction.toSimplifiedResponse(): AuctionSimplifiedResponse = AuctionSimplifiedResponse(id, name, category, price)
