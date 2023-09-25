package pl.kawaleria.auctsys.auctions.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query

class MongoAuctionSearchRepository(val mongoTemplate: MongoTemplate) : AuctionSearchRepository {
    override fun search(query: Query, pageable: Pageable): Page<Auction> {
        val auctions = mongoTemplate.find(query, Auction::class.java).toList()
        val count = mongoTemplate.count(query, Auction::class.java)
        return PageImpl(auctions, pageable, count)
    }
}