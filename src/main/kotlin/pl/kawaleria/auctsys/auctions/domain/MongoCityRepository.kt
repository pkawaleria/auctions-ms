package pl.kawaleria.auctsys.auctions.domain

import org.springframework.data.mongodb.repository.MongoRepository

interface MongoCityRepository : CityRepository, MongoRepository<City, String> {
}