package pl.kawaleria.auctsys.views.domain

import java.util.Optional


interface AuctionViewsRepository {
    fun save(auctionViews: AuctionViews): AuctionViews
    fun findById(id: String): Optional<AuctionViews>
}
