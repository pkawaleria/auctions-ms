package pl.kawaleria.auctsys.requests

import pl.kawaleria.auctsys.models.Category

data class UpdateAuctionDto(
    val name: String,
    val category: Category,
    val description: String,
    val price: Double
)
