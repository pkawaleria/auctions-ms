package pl.kawaleria.auctsys.auctions.dto.requests

data class AuctionsSearchRequest(
        val searchPhrase: String?,
        val categoryName: String?,
        val categoryId: String?,
        val cityId: String?,
        val radius: Double?,
        val province: String?,
        val priceFrom: Int?,
        val priceTo: Int?,
        val sortOrder: SortOrder?,
        val sortBy: AuctionsSortBy?
)