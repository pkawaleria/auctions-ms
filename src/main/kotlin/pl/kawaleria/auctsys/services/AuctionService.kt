package pl.kawaleria.auctsys.services

import org.springframework.stereotype.Service
import pl.kawaleria.auctsys.models.Auction
import pl.kawaleria.auctsys.repositories.AuctionRepository

@Service
class AuctionService(private val auctionRepository: AuctionRepository) {
    fun findAuctionsByAuctioneerId(auctioneerId: String): MutableList<Auction> = auctionRepository.findAuctionsByAuctioneerId(auctioneerId)
    fun findAuctionByIdAndAuctioneerId(id: String, auctioneerId: String): Auction? = auctionRepository.findAuctionByIdAndAuctioneerId(id, auctioneerId)
    fun save(auction: Auction): Auction = auctionRepository.save(auction)
    fun delete(auction: Auction): Unit = auctionRepository.delete(auction)
}