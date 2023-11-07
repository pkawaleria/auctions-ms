package pl.kawaleria.auctsys.auctions.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query

class MongoAuctionSearchRepository(private val mongoTemplate: MongoTemplate) : AuctionSearchRepository {
    override fun search(query: Query, pageable: Pageable): Page<Auction> {
        val count: Long = mongoTemplate.count(query, Auction::class.java)
        query.with(pageable)
        val auctions: List<Auction> = mongoTemplate.find(query, Auction::class.java).toList()
        return PageImpl(auctions, pageable, count)
    }
}