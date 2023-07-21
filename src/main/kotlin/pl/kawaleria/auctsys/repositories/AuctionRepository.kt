package pl.kawaleria.auctsys.repositories

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import pl.kawaleria.auctsys.models.Auction
import pl.kawaleria.auctsys.models.Category

interface AuctionRepository : MongoRepository<Auction, String> {
    fun findAuctionsByAuctioneerId(auctioneerId: String): MutableList<Auction>
    fun findAuctionByIdAndAuctioneerId(id: String, auctioneerId: String): Auction?
    fun findByNameContainingIgnoreCaseAndCategoryEquals(name: String, category: Category, pageable: Pageable) : Page<Auction>
    fun findByCategoryEquals(mappedCategory: Category, pageable: Pageable): Page<Auction>
    fun findByNameContainingIgnoreCase(searchPhrase: String, pageRequest: Pageable): Page<Auction>
}