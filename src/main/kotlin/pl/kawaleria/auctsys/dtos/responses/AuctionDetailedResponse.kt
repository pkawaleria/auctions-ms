package pl.kawaleria.auctsys.dtos.responses

import pl.kawaleria.auctsys.models.Auction
import pl.kawaleria.auctsys.models.Category

data class AuctionDetailedResponse(
    val id: String?,
    val name: String?,
    val category: Category?,
    val description: String?,
    val price: Double?,
    val auctioneerId: String?
)

fun Auction.toDto(): AuctionDetailedResponse = AuctionDetailedResponse(id, name, category, description, price, auctioneerId)
