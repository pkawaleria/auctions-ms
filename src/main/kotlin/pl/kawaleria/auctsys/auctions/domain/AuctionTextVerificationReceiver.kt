package pl.kawaleria.auctsys.auctions.domain

import pl.kawaleria.auctsys.auctions.dto.events.AuctionTextVerificationReceivedEvent

interface AuctionTextVerificationReceiver {
    fun handleVerification(verification: AuctionTextVerificationReceivedEvent)
}