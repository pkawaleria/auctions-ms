package pl.kawaleria.auctsys.auctions.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.Instant
import java.util.*

interface AuctionRepository {
    fun findActiveAuctionsByAuctioneerId(auctioneerId: String, now: Instant): List<Auction>
    fun findAuctionsByAuctioneerId(auctioneerId: String, pageable: Pageable): Page<Auction>
    fun findAuctionsWithCategoryInPath(categoryName: String, pageable: Pageable): Page<Auction>
    fun save(auction: Auction): Auction
    fun findById(id: String): Optional<Auction>
    fun deleteById(auctionId: String)
    fun existsById(auctionId: String): Boolean
    fun delete(auction: Auction)
    fun findRejectedAuctions(auctioneerId: String, pageable: Pageable): Page<Auction>
    fun findAcceptedAuctions(auctioneerId: String, pageable: Pageable): Page<Auction>
    fun findExpiredAuctions(now: Instant, auctioneerId: String, pageable: Pageable): Page<Auction>
    fun findArchivedAuctions(auctioneerId: String, pageable: Pageable): Page<Auction>
    fun findAwaitingAcceptanceAuctions(auctioneerId: String, pageable: Pageable): Page<Auction>
    fun findActiveAuction(id: String, now: Instant): Optional<Auction>
    fun findByIdIn(ids: List<String>): List<Auction>
}