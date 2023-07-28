package pl.kawaleria.auctsys.auctions.dto.responses

import pl.kawaleria.auctsys.auctions.domain.Auction
import pl.kawaleria.auctsys.auctions.domain.Category

data class AuctionDetailedResponse(
        val id: String?,
        val name: String?,
        val category: Category?,
        val description: String?,
        val price: Double?,
        val auctioneerId: String?
)

fun Auction.toDetailedResponse(): AuctionDetailedResponse = AuctionDetailedResponse(id, name, category, description, price, auctioneerId)
