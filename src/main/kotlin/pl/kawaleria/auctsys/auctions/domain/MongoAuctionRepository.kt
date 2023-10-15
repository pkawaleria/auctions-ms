package pl.kawaleria.auctsys.auctions.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import java.time.Instant
import java.util.*

interface MongoAuctionRepository : AuctionRepository, MongoRepository<Auction, String> {

    @Query("{'id': ?0, 'status': 'ACCEPTED', 'expiresAt': { \$gte: ?1 }}")
    override fun findActiveAuction(id: String, now: Instant): Optional<Auction>
    @Query("{'status': 'ACCEPTED', 'expiresAt': { \$gte: ?1 }, 'auctioneerId': ?0}")
    override fun findActiveAuctionsByAuctioneerId(auctioneerId: String, now: Instant): List<Auction>

    @Query("{ 'categoryPath.pathElements.name': ?0 }")
    override fun findAuctionsWithCategoryInPath(categoryName: String, pageable: Pageable): Page<Auction>

    @Query("{'status': 'REJECTED', 'auctioneerId' : ?0}")
    override fun findRejectedAuctions(auctioneerId: String, pageable: Pageable): Page<Auction>

    @Query("{'status': 'ACCEPTED', 'auctioneerId' : ?0}")
    override fun findAcceptedAuctions(auctioneerId: String, pageable: Pageable): Page<Auction>

    @Query("{'expiresAt' : {'\$lt' : ?0 }, 'auctioneerId' : ?1 }")
    override fun findExpiredAuctions(now: Instant, auctioneerId: String, pageable: Pageable): Page<Auction>

    @Query("{'status': 'ARCHIVED', 'auctioneerId' : ?0}")
    override fun findArchivedAuctions(auctioneerId: String, pageable: Pageable): Page<Auction>

    @Query("{'status': 'NEW', 'auctioneerId' : ?0}")
    override fun findAwaitingAcceptanceAuctions(auctioneerId: String, pageable: Pageable): Page<Auction>
}