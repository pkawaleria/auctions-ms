package pl.kawaleria.auctsys.dtos.requests

import pl.kawaleria.auctsys.models.Category

data class CreateAuctionRequest(
    val name: String,
    val category: Category,
    val description: String,
    val price: Double
)
