package pl.kawaleria.auctsys.auctions.dto.requests

data class CreateAuctionRequest(
        val name: String,
        val description: String,
        val price: Double,
        val categoryId: String
)
