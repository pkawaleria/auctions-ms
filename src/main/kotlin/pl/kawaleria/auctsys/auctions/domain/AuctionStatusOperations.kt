package pl.kawaleria.auctsys.auctions.domain

interface AuctionStatusOperations {
    fun accept(auction: Auction): AuctionStatus
    fun reject(auction: Auction): AuctionStatus
    fun archive(auction: Auction): AuctionStatus
}