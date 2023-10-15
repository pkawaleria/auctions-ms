package pl.kawaleria.auctsys.auctions.domain

import org.springframework.context.ApplicationEventPublisher
import pl.kawaleria.auctsys.auctions.dto.events.AuctionViewedEvent

class SpringAuctionEventPublisher(private val eventPublisher: ApplicationEventPublisher) : AuctionDomainEventPublisher {
    override fun publishAuctionView(auctionViewedEvent: AuctionViewedEvent) {
        eventPublisher.publishEvent(auctionViewedEvent)
    }

}