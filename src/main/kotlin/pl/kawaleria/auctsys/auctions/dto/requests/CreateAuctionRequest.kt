package pl.kawaleria.auctsys.auctions.dto.requests

import pl.kawaleria.auctsys.auctions.domain.Condition

data class CreateAuctionRequest(
        val name: String,
        val description: String,
        val price: Double,
        val categoryId: String,
        val cityId: String,
        val productCondition: Condition
)
