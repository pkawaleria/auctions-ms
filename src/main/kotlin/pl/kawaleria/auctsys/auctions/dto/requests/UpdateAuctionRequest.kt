package pl.kawaleria.auctsys.auctions.dto.requests

import pl.kawaleria.auctsys.auctions.domain.Condition

data class UpdateAuctionRequest(
    val name: String,
    val description: String,
    val price: Double,
    val productCondition: Condition,
    val cityId: String,
)
