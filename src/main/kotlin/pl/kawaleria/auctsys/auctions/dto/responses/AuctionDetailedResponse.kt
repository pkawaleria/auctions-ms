package pl.kawaleria.auctsys.auctions.dto.responses

import pl.kawaleria.auctsys.auctions.domain.Auction
import pl.kawaleria.auctsys.auctions.domain.Category
import pl.kawaleria.auctsys.auctions.domain.CategoryPath
import pl.kawaleria.auctsys.auctions.domain.Condition
import java.time.Instant

data class AuctionDetailedResponse(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val auctioneerId: String,
    val thumbnail: ByteArray,
    val category: Category,
    val categoryPath: CategoryPath,
    val productCondition: Condition,
    val cityId: String,
    val cityName: String,
    val province: String,
    val longitude: Double,
    val latitude: Double,
    val expirationTimestamp: Instant,
    val status: String,
    val viewCount: Long,
    val phoneNumber: String
)

fun Auction.toDetailedResponse(viewCount: Long = 0L): AuctionDetailedResponse = AuctionDetailedResponse(
    id, name, description, price, auctioneerId, thumbnail, category, categoryPath, productCondition,
    cityId, cityName, province, location.x, location.y, expiresAt, status.name, viewCount, phoneNumber
)
