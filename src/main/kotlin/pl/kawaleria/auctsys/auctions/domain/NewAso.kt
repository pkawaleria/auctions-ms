package pl.kawaleria.auctsys.auctions.domain

class NewAso : AuctionStatusOperations {
    override fun accept(auction: Auction): AuctionStatus {
        return AuctionStatus.ACCEPTED
    }

    override fun reject(auction: Auction): AuctionStatus {
        return AuctionStatus.REJECTED
    }

    override fun archive(auction: Auction): AuctionStatus {
        return AuctionStatus.ARCHIVED
    }
}