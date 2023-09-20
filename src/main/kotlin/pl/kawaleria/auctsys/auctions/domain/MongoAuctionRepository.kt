package pl.kawaleria.auctsys.auctions.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import java.time.Instant

interface MongoAuctionRepository : AuctionRepository, MongoRepository<Auction, String> {
    @Query("{ 'categoryPath.pathElements.name': ?0 }")
    override fun findAuctionsWithCategoryInPath(categoryName: String, pageable: Pageable): Page<Auction>

    @Query("{ 'name': { \$regex: ?0, \$options: 'i' }, 'categoryPath.pathElements.name': ?1 }")
    override fun findByNameContainingIgnoreCaseAndCategoryPathContaining(name: String, categoryName: String, pageable: Pageable): Page<Auction>

    @Query("{'status': AuctionStatus.REJECTED, 'auctioneerId' : ?0}")
    override fun findRejectedAuctions(auctioneerId: String, pageable: Pageable): Page<Auction>

    @Query("{'status': AuctionStatus.ACCEPTED, 'auctioneerId' : ?0}")
    override fun findAcceptedAuctions(auctioneerId: String, pageable: Pageable): Page<Auction>

    @Query("{'expiresAt' : {'\$lt' : ?0 }, 'auctioneerId' : ?1 }")
    override fun findExpiredAuctions(now: Instant, auctioneerId: String, pageable: Pageable): Page<Auction>

    @Query("{'status': AuctionStatus.ARCHIVED, 'auctioneerId' : ?0}")
    override fun findArchivedAuctions(auctioneerId: String, pageable: Pageable): Page<Auction>

    @Query("{'status': AuctionStatus.NEW, 'auctioneerId' : ?0}")
    override fun findAwaitingAcceptanceAuctions(auctioneerId: String, pageable: Pageable): Page<Auction>
}