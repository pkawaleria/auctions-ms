package pl.kawaleria.auctsys.auctions.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Point
import java.time.Instant
import java.util.*

interface AuctionRepository {
    fun findAuctionsByAuctioneerId(auctioneerId: String): MutableList<Auction>
    fun findByNameContainingIgnoreCaseAndCategoryPathContaining(name: String, categoryName: String, pageable: Pageable): Page<Auction>
    fun findAuctionsWithCategoryInPath(categoryName: String, pageable: Pageable): Page<Auction>
    fun findByNameContainingIgnoreCase(searchPhrase: String, pageable: Pageable): Page<Auction>
    fun findAuctionsByCityId(cityId: String, pageable: Pageable): Page<Auction>
    fun findByLocationNear(location: Point, distance: Distance, pageable: Pageable): Page<Auction>
    fun save(auction: Auction): Auction
    fun findById(id: String): Optional<Auction>
    fun findAll(pageRequest: Pageable): Page<Auction>
    fun deleteById(auctionId: String)
    fun existsById(auctionId: String): Boolean
    fun delete(auction: Auction)
    fun findRejectedAuctions(auctioneerId: String, pageable: Pageable): Page<Auction>
    fun findAcceptedAuctions(auctioneerId: String, pageable: Pageable): Page<Auction>
    fun findExpiredAuctions(now: Instant, auctioneerId: String, pageable: Pageable): Page<Auction>
    fun findArchivedAuctions(auctioneerId: String, pageable: Pageable): Page<Auction>
    fun findAwaitingAcceptanceAuctions(auctioneerId: String, pageable: Pageable): Page<Auction>
}