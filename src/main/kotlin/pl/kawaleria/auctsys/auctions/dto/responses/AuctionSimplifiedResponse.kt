package pl.kawaleria.auctsys.auctions.dto.responses

import org.springframework.data.mongodb.core.geo.GeoJsonPoint
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
        val location: GeoJsonPoint
)

fun Auction.toSimplifiedResponse(): AuctionSimplifiedResponse = AuctionSimplifiedResponse(id, name, category, categoryPath, price, thumbnail, cityName, location)
