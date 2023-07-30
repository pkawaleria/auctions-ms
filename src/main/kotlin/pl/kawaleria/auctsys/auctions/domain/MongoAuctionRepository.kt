package pl.kawaleria.auctsys.auctions.domain

import org.springframework.data.mongodb.repository.MongoRepository

interface MongoAuctionRepository : AuctionRepository, MongoRepository<Auction, String> {

}