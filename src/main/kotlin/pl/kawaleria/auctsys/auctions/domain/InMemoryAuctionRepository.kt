package pl.kawaleria.auctsys.auctions.domain

import org.bson.types.ObjectId
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Point
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class InMemoryAuctionRepository : AuctionRepository {

    val map: ConcurrentHashMap<String, Auction> = ConcurrentHashMap()
    override fun findAuctionsByAuctioneerId(auctioneerId: String): MutableList<Auction> {
        return map.values.filter { it.auctioneerId == auctioneerId }.toMutableList()
    }

    override fun findByNameContainingIgnoreCaseAndCategoryPathContaining(
        name: String,
        categoryName: String,
        pageable: Pageable
    ): Page<Auction> {
        val filteredAuctions: MutableList<Auction> = map.values
            .filter { auction -> auction.categoryPath.containsCategoryOfName(categoryName) }
            .filter { it.name.contains(name, ignoreCase = true) }
            .toMutableList()
        return PageImpl(filteredAuctions, pageable, filteredAuctions.size.toLong())
    }

    override fun findAuctionsWithCategoryInPath(categoryName: String, pageable: Pageable): Page<Auction> {
        val filteredAuctions: MutableList<Auction> =
            map.values.filter { e -> e.categoryPath.containsCategoryOfName(categoryName) }.toMutableList()
        return PageImpl(filteredAuctions, pageable, filteredAuctions.size.toLong())
    }

    override fun findByNameContainingIgnoreCase(searchPhrase: String, pageable: Pageable): Page<Auction> {
        val filteredAuctions: MutableList<Auction> = map.values
            .filter { it.name?.contains(searchPhrase, ignoreCase = true) ?: false }
            .toMutableList()
        return PageImpl(filteredAuctions, pageable, filteredAuctions.size.toLong())
    }

    override fun findAuctionsByCityId(cityId: String, pageable: Pageable): Page<Auction> {
        val filteredAuctions: MutableList<Auction> = map.values.filter { it.cityId == cityId }.toMutableList()

        return PageImpl(filteredAuctions, pageable, filteredAuctions.size.toLong())
    }

    override fun findByLocationNear(location: Point, distance: Distance, pageable: Pageable): Page<Auction> {
        val filteredAuctions: MutableList<Auction> = map.values.filter { auction ->
            val calculatedDistance = haversineDistance(firstLocation = location, secondLocation = auction.location)
            calculatedDistance <= distance.value
        }.toMutableList()
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

    private fun haversineDistance(firstLocation: Point, secondLocation: Point): Double {
        val R = 6371e3  // radius of Earth in meters
        val φ1 = Math.toRadians(firstLocation.x)
        val φ2 = Math.toRadians(secondLocation.x)
        val Δφ = Math.toRadians(secondLocation.x - firstLocation.x)
        val Δλ = Math.toRadians(secondLocation.y - firstLocation.y)

        val a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
                Math.cos(φ1) * Math.cos(φ2) *
                Math.sin(Δλ / 2) * Math.sin(Δλ / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return R * c
    }
}