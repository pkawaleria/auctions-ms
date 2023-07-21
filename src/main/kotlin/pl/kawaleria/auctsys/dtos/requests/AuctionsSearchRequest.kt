package pl.kawaleria.auctsys.dtos.requests

data class AuctionsSearchRequest(
        val searchPhrase: String?,
        val category: String?
)