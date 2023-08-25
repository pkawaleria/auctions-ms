package pl.kawaleria.auctsys.auctions.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

interface AuctionRepository {
    fun findAuctionsByAuctioneerId(auctioneerId: String): MutableList<Auction>
    fun findByNameContainingIgnoreCaseAndCategoryPathContaining(name: String, categoryName: String, pageable: Pageable): Page<Auction>
    fun findAuctionsWithCategoryInPath(categoryName: String, pageable: Pageable): Page<Auction>
    fun findByNameContainingIgnoreCase(searchPhrase: String, pageRequest: Pageable): Page<Auction>
    fun save(auction: Auction): Auction
    fun findById(id: String): Optional<Auction>
    fun findAll(pageRequest: Pageable): Page<Auction>
    fun deleteById(auctionId: String)
    fun existsById(auctionId: String): Boolean
    fun delete(auction: Auction)
}