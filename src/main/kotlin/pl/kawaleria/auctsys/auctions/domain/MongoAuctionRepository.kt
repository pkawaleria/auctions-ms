package pl.kawaleria.auctsys.auctions.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface MongoAuctionRepository : AuctionRepository, MongoRepository<Auction, String> {
    @Query("{ 'categoryPath.pathElements.name': ?0 }")
    override fun findAuctionsWithCategoryInPath(categoryName: String, pageable: Pageable): Page<Auction>

    @Query("{ 'name': { \$regex: ?0, \$options: 'i' }, 'categoryPath.pathElements.name': ?1 }")
    override fun findByNameContainingIgnoreCaseAndCategoryPathContaining(name: String, categoryName: String, pageable: Pageable): Page<Auction>
}