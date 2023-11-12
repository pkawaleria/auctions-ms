package pl.kawaleria.auctsys.auctions.dto.responses

import pl.kawaleria.auctsys.auctions.domain.Auction
import pl.kawaleria.auctsys.auctions.domain.Category
import pl.kawaleria.auctsys.auctions.domain.CategoryPath

data class AuctionSimplifiedResponse(
    val id: String,
    val name: String,
    val category: Category,
    val categoryPath: CategoryPath,
    val price: Double,
    val thumbnail: ByteArray,
    val cityName: String,
    val province: String,
    val viewCounter: Long = 0
)

fun Auction.toSimplifiedResponse(): AuctionSimplifiedResponse =
    AuctionSimplifiedResponse(id, name, category, categoryPath, price, thumbnail, cityName, province)

fun Auction.toSimplifiedResponse(viewCounter: Long): AuctionSimplifiedResponse =
    AuctionSimplifiedResponse(id, name, category, categoryPath, price, thumbnail, cityName, province, viewCounter)
