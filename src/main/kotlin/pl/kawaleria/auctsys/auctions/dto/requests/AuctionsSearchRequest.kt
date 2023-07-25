package pl.kawaleria.auctsys.auctions.dto.requests

data class AuctionsSearchRequest(
        val searchPhrase: String?,
        val category: String?
)