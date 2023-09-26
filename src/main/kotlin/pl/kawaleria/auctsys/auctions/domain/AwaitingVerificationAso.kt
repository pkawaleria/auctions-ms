package pl.kawaleria.auctsys.auctions.domain

class AwaitingVerificationAso : AuctionStatusOperations {

    override fun accept(auction: Auction): AuctionStatus = AuctionStatus.AWAITING_VERIFICATION

    override fun reject(auction: Auction): AuctionStatus = AuctionStatus.AWAITING_VERIFICATION

    override fun archive(auction: Auction): AuctionStatus = AuctionStatus.AWAITING_VERIFICATION
}