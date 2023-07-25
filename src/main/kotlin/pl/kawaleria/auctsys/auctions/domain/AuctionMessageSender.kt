package pl.kawaleria.auctsys.auctions.domain

interface AuctionMessageSender {
    fun sendMessageForCreated(auction: Auction)
    fun sendMessageForArchived(auction: Auction)
}