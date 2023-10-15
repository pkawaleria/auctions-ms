package pl.kawaleria.auctsys.auctions.domain

import pl.kawaleria.auctsys.auctions.dto.events.AuctionViewedEvent

interface AuctionDomainEventPublisher {
    fun publishAuctionView(auctionViewedEvent: AuctionViewedEvent)
}