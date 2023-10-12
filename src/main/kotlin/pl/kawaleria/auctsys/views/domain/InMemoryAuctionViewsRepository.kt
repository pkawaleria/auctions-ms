package pl.kawaleria.auctsys.views.domain

import java.util.*
import java.util.concurrent.ConcurrentHashMap

class InMemoryAuctionViewsRepository : AuctionViewsRepository {

    val map: ConcurrentHashMap<String, AuctionViews> = ConcurrentHashMap()
    override fun save(auctionViews: AuctionViews): AuctionViews {
        map[auctionViews.auctionId] = auctionViews
        return auctionViews
    }

    override fun findById(id: String): Optional<AuctionViews> {
        return Optional.ofNullable(map[id])
    }

}
