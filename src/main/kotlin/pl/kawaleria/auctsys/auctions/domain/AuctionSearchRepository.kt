package pl.kawaleria.auctsys.auctions.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.query.Query

interface AuctionSearchRepository {
    fun search(query: Query, pageable: Pageable): Page<Auction>
}