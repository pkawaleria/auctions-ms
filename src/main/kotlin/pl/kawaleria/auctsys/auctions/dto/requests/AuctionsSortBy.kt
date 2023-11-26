package pl.kawaleria.auctsys.auctions.dto.requests

enum class AuctionsSortBy(val auctionProperty: String) {
    NAME("name"), CREATED_AT("createdAt"), PRICE("price")
}

enum class SortOrder {
    ASC, DESC
}