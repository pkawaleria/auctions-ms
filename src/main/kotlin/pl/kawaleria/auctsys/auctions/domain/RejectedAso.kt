package pl.kawaleria.auctsys.auctions.domain

import pl.kawaleria.auctsys.auctions.dto.exceptions.UnsupportedOperationOnAuctionException

class RejectedAso : AuctionStatusOperations {
    override fun accept(auction: Auction): AuctionStatus =
            throw UnsupportedOperationOnAuctionException(AuctionStatus.ACCEPTED.operationName, AuctionStatus.REJECTED.statusName)

    override fun reject(auction: Auction): AuctionStatus = AuctionStatus.REJECTED

    override fun archive(auction: Auction): AuctionStatus = AuctionStatus.ARCHIVED

}