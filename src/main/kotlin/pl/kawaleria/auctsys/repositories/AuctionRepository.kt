package pl.kawaleria.auctsys.repositories

import org.springframework.data.mongodb.repository.MongoRepository
import pl.kawaleria.auctsys.models.Auction

interface AuctionRepository : MongoRepository<Auction, String> {
    fun findAuctionsByAuctioneerId(auctioneerId: String): MutableList<Auction>
    fun findAuctionByIdAndAuctioneerId(id: String, auctioneerId: String): Auction?
}