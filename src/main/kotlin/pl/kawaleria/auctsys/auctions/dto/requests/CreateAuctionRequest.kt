package pl.kawaleria.auctsys.auctions.dto.requests

import pl.kawaleria.auctsys.auctions.domain.Category

data class CreateAuctionRequest(
        val name: String,
        val category: Category,
        val description: String,
        val price: Double
)
