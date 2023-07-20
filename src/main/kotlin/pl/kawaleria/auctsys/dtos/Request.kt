package pl.kawaleria.auctsys.dtos

import pl.kawaleria.auctsys.models.Category

data class CreateAuctionDto(
    val name: String,
    val category: Category,
    val description: String,
    val price: Double
)

data class UpdateAuctionDto(
    val name: String,
    val category: Category,
    val description: String,
    val price: Double
)