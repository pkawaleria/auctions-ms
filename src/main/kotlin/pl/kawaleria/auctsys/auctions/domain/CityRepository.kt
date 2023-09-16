package pl.kawaleria.auctsys.auctions.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository

interface CityRepository : MongoRepository<City, String> {

    fun findByNameContainingIgnoreCase(searchCityName: String, pageRequest: Pageable): Page<City>
}