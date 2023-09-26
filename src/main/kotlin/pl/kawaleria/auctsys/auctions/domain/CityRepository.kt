package pl.kawaleria.auctsys.auctions.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.*

interface CityRepository {

    fun findByNameContainingIgnoreCase(searchCityName: String, pageRequest: Pageable): Page<City>
    fun count(): Long
    fun <S : City?> saveAll(entities: MutableIterable<S?>): List<City>
    fun deleteAll()
    fun findAll(pageable: Pageable): Page<City>
    fun findById(cityId: String): Optional<City>
    fun save(city: City): City

}
