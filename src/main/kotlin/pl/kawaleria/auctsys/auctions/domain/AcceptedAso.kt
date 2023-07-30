package pl.kawaleria.auctsys.auctions.domain

import pl.kawaleria.auctsys.auctions.dto.exceptions.UnsupportedOperationOnAuctionException

class AcceptedAso : AuctionStatusOperations {
    override fun accept(auction: Auction): AuctionStatus {
        throw UnsupportedOperationOnAuctionException(AuctionStatus.ACCEPTED.operationName, AuctionStatus.ACCEPTED.statusName)
    }

    override fun reject(auction: Auction): AuctionStatus {
        return AuctionStatus.REJECTED
    }

    override fun archive(auction: Auction): AuctionStatus {
        throw return AuctionStatus.ARCHIVED
    }
}