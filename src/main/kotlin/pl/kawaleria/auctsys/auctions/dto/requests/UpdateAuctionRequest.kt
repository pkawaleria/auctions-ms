package pl.kawaleria.auctsys.auctions.dto.requests

data class UpdateAuctionRequest(
        val name: String,
        val description: String,
        val price: Double
)
