package pl.kawaleria.auctsys.auctions.domain

class AcceptedAso : AuctionStatusOperations {
    override fun accept(auction: Auction): AuctionStatus = AuctionStatus.ACCEPTED

    override fun reject(auction: Auction): AuctionStatus = AuctionStatus.REJECTED

    override fun archive(auction: Auction): AuctionStatus = AuctionStatus.ARCHIVED

}