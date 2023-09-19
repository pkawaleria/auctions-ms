package pl.kawaleria.auctsys.auctions.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Point
import java.util.*

interface AuctionRepository {
    fun findAuctionsByAuctioneerId(auctioneerId: String): MutableList<Auction>
    fun findByNameContainingIgnoreCaseAndCategoryPathContaining(name: String, categoryName: String, pageable: Pageable): Page<Auction>
    fun findAuctionsWithCategoryInPath(categoryName: String, pageable: Pageable): Page<Auction>
    fun findByNameContainingIgnoreCase(searchPhrase: String, pageable: Pageable): Page<Auction>
    fun findAuctionsByCityId(cityId: String, pageable: Pageable): Page<Auction>
    fun findByLocationNear(startPoint: Point, distance: Distance, pageable: Pageable): Page<Auction>
    fun save(auction: Auction): Auction
    fun findById(id: String): Optional<Auction>
    fun findAll(pageable: Pageable): Page<Auction>
    fun deleteById(auctionId: String)
    fun existsById(auctionId: String): Boolean
    fun delete(auction: Auction)
    fun deleteAll()
    fun <S : Auction?> saveAll(entities: MutableIterable<S>) : List<Auction>
}