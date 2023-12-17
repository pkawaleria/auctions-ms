package pl.kawaleria.auctsys.auctions.domain

import pl.kawaleria.auctsys.auctions.dto.events.VerifyAuctionTextRequestEvent

interface AuctionMessageSender {
    fun sendToVerification(event: VerifyAuctionTextRequestEvent)
}