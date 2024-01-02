package pl.kawaleria.auctsys.auctions.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.query.Query
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class InMemoryAuctionRepository : AuctionRepository, AuctionSearchRepository {

    val map: ConcurrentHashMap<String, Auction> = ConcurrentHashMap()
    override fun findActiveAuctionsByAuctioneerId(auctioneerId: String, now: Instant): MutableList<Auction> {
        return map.values.filter { it.auctioneerId == auctioneerId && it.isActive(now) }.toMutableList()
    }

    override fun findAuctionsByAuctioneerId(auctioneerId: String, pageable: Pageable): Page<Auction> {
        val filteredAuctions: List<Auction> = map.values.filter { it.auctioneerId == auctioneerId }
        return PageImpl(filteredAuctions, pageable, filteredAuctions.size.toLong())
    }

    override fun findAuctionsWithCategoryInPath(categoryName: String, pageable: Pageable): Page<Auction> {
        val filteredAuctions: MutableList<Auction> =
            map.values.filter { e -> e.categoryPath.containsCategoryOfName(categoryName) }.toMutableList()
        return PageImpl(filteredAuctions, pageable, filteredAuctions.size.toLong())
    }

    override fun save(auction: Auction): Auction {
        map[auction.id] = auction
        return auction
    }

    override fun findById(id: String): Optional<Auction> = Optional.ofNullable(map[id])

    override fun deleteById(auctionId: String) {
        map.remove(auctionId)
    }

    override fun existsById(auctionId: String): Boolean {
        return map.containsKey(auctionId)
    }

    override fun delete(auction: Auction) {
        map.remove(auction.id)
    }

    override fun findRejectedAuctions(auctioneerId: String, pageable: Pageable): Page<Auction> {
        val filteredAuctions: MutableList<Auction> = map.values
            .filter { it.status == AuctionStatus.REJECTED }
            .toMutableList()
        return PageImpl(filteredAuctions, pageable, filteredAuctions.size.toLong())
    }

    override fun findAwaitingAcceptanceAuctions(auctioneerId: String, pageable: Pageable): Page<Auction> {
        val filteredAuctions: MutableList<Auction> = map.values
            .filter { it.status == AuctionStatus.NEW }
            .toMutableList()
        return PageImpl(filteredAuctions, pageable, filteredAuctions.size.toLong())
    }

    override fun findActiveAuction(id: String, now: Instant): Optional<Auction> {
        return Optional.ofNullable(map[id]).filter { it.isActive(now) }
    }

    override fun findByIdIn(ids: List<String>): List<Auction> {
        return ids.mapNotNull { findById(it).orElse(null) }
    }

    override fun findArchivedAuctions(auctioneerId: String, pageable: Pageable): Page<Auction> {
        val filteredAuctions: MutableList<Auction> = map.values
            .filter { it.status == AuctionStatus.ARCHIVED }
            .toMutableList()
        return PageImpl(filteredAuctions, pageable, filteredAuctions.size.toLong())
    }

    override fun findAcceptedAuctions(auctioneerId: String, pageable: Pageable): Page<Auction> {
        val filteredAuctions: MutableList<Auction> = map.values
            .filter { it.status == AuctionStatus.ACCEPTED }
            .toMutableList()
        return PageImpl(filteredAuctions, pageable, filteredAuctions.size.toLong())
    }

    override fun findExpiredAuctions(now: Instant, auctioneerId: String, pageable: Pageable): Page<Auction> {
        val filteredAuctions: MutableList<Auction> = map.values
            .filter { it.expiresAt.isBefore(Instant.now()) }
            .toMutableList()
        return PageImpl(filteredAuctions, pageable, filteredAuctions.size.toLong())
    }

    // this method is just mocked, it should not be used since search concept should be tested only in integration tests
    override fun search(query: Query, pageable: Pageable): Page<Auction> {
        return PageImpl(emptyList(), pageable, 0L)
    }
}