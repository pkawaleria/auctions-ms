package pl.kawaleria.auctsys.auctions.dto.responses

import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import pl.kawaleria.auctsys.auctions.domain.Auction
import pl.kawaleria.auctsys.auctions.domain.Category
import pl.kawaleria.auctsys.auctions.domain.Condition
import pl.kawaleria.auctsys.auctions.domain.Location

data class AuctionDetailedResponse(
        val id: String?,
        val name: String?,
        val description: String?,
        val price: Double?,
        val auctioneerId: String?,
        val thumbnail: ByteArray?,
        val category: Category?,
        val productCondition: Condition?,
        val cityId: String?,
        val cityName: String?,
        val location: GeoJsonPoint?
)

fun Auction.toDetailedResponse(): AuctionDetailedResponse = AuctionDetailedResponse(id, name, description, price, auctioneerId, thumbnail, category, productCondition, cityId, cityName, location)
