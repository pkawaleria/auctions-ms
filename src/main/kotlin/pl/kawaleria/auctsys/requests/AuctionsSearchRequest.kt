package pl.kawaleria.auctsys.requests

data class AuctionsSearchRequest(
        val searchPhrase: String?,
        val category: String?
)