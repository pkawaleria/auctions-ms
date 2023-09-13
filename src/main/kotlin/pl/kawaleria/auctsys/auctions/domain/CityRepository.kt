package pl.kawaleria.auctsys.auctions.domain

import org.springframework.data.mongodb.repository.MongoRepository

interface CityRepository : MongoRepository<City, String>