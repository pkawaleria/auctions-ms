package pl.kawaleria.auctsys.auctions.domain

import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class InMemoryAuctionRepository : AuctionRepository {

    val map: ConcurrentHashMap<String, Auction> = ConcurrentHashMap()
    override fun findAuctionsByAuctioneerId(auctioneerId: String): MutableList<Auction> {
        return map.values.filter { it.auctioneerId == auctioneerId }.toMutableList()
    }

    override fun findByNameContainingIgnoreCaseAndCategoryPathContaining(name: String, categoryName: String, pageable: Pageable): Page<Auction> {
        val filteredAuctions: MutableList<Auction> = map.values
                .filter { auction -> auction.categoryPath.containsCategoryOfName(categoryName) }
                .filter { it.name?.contains(name, ignoreCase = true) ?: false }
                .toMutableList()
        return PageImpl(filteredAuctions, pageable, filteredAuctions.size.toLong())
    }

    override fun findAuctionsWithCategoryInPath(categoryName: String, pageable: Pageable): Page<Auction> {
        val filteredAuctions: MutableList<Auction> = map.values.filter { e -> e.categoryPath.containsCategoryOfName(categoryName) }.toMutableList()
        return PageImpl(filteredAuctions, pageable, filteredAuctions.size.toLong())
    }

    override fun findByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<Auction> {
        val filteredAuctions: MutableList<Auction> = map.values
                .filter { it.name?.contains(name, ignoreCase = true) ?: false }
                .toMutableList()
        return PageImpl(filteredAuctions, pageable, filteredAuctions.size.toLong())
    }

    override fun save(auction: Auction): Auction {
        if (auction.id == null) {
            // If the auction doesn't have an ID, it's a new auction, generate a new ID.
            val auctionId: String = ObjectId().toString()
            auction.id = auctionId
        }
        // Save the auction with the generated ID or the existing ID (if it's an update).
        map[auction.id!!] = auction
        return auction
    }

    override fun findById(id: String): Optional<Auction> = Optional.ofNullable(map[id])

    override fun findAll(pageable: Pageable): Page<Auction> {
        val allAuctions: List<Auction> = map.values.toList()
        return PageImpl(allAuctions, pageable, allAuctions.size.toLong())
    }

    override fun deleteById(auctionId: String) {
        map.remove(auctionId)
    }

    override fun existsById(auctionId: String): Boolean {
        return map.containsKey(auctionId)
    }

    override fun delete(auction: Auction) {
        map.remove(auction.id)
    }
}