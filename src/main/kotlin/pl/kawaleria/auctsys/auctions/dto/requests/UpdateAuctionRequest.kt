package pl.kawaleria.auctsys.auctions.dto.requests

import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import pl.kawaleria.auctsys.auctions.domain.Condition

data class UpdateAuctionRequest(
        val name: String,
        val description: String,
        val price: Double,
        val productCondition: Condition,
        val cityId: String,
        val cityName: String,
        val location: GeoJsonPoint
)
