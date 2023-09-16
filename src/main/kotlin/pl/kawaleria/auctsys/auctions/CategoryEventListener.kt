package pl.kawaleria.auctsys.auctions

import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import pl.kawaleria.auctsys.auctions.domain.AuctionFacade
import pl.kawaleria.auctsys.categories.dto.events.CategoryDeletedEvent

@Component
class CategoryEventListener(private val auctionFacade: AuctionFacade) {

    @EventListener
    @Async
    fun handle(categoryDeleted: CategoryDeletedEvent) {
        auctionFacade.eraseCategoryFromAuctions(categoryName = categoryDeleted.categoryName)
    }

}