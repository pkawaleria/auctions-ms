package pl.kawaleria.auctsys.auctions.dto.requests

data class AuctionsSearchRequest(
        val searchPhrase: String?,
        val categoryName: String?,
        val categoryId: String?,
        val cityId: String?,
        val radius: Double?
)