package pl.kawaleria.auctsys.images.domain

import org.springframework.data.mongodb.repository.MongoRepository

interface ImageRepository : MongoRepository<Image, String> {
    fun findImagesByAuctionId(auctionId: String): List<Image>
    fun save(image: Image): Image
    fun deleteAllByAuctionId(auctionId: String)
}