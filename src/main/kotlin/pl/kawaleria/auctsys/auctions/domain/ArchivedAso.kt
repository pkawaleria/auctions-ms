package pl.kawaleria.auctsys.auctions.domain

import pl.kawaleria.auctsys.auctions.dto.exceptions.UnsupportedOperationOnAuctionException

class ArchivedAso : AuctionStatusOperations {
    override fun accept(auction: Auction): AuctionStatus = AuctionStatus.ACCEPTED

    override fun reject(auction: Auction): AuctionStatus =
            throw UnsupportedOperationOnAuctionException(AuctionStatus.REJECTED.operationName, AuctionStatus.ARCHIVED.statusName)

    override fun archive(auction: Auction): AuctionStatus =
            throw UnsupportedOperationOnAuctionException(AuctionStatus.ARCHIVED.operationName, AuctionStatus.ARCHIVED.statusName)
}