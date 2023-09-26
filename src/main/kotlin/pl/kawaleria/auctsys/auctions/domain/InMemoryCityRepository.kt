package pl.kawaleria.auctsys.auctions.domain

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class InMemoryCityRepository : CityRepository {

    val map: ConcurrentHashMap<String, City> = ConcurrentHashMap()

    override fun findByNameContainingIgnoreCase(searchCityName: String, pageRequest: Pageable): Page<City> {
        val filteredCities: List<City> = map.values
            .filter { it.name.contains(searchCityName, ignoreCase = true) }
            .toList()
        return PageImpl(filteredCities, pageRequest, filteredCities.size.toLong())
    }

    override fun count(): Long {
        return map.count().toLong()
    }

    override fun <S : City?> saveAll(entities: MutableIterable<S?>): List<City> {
        return entities.map { save(it!!) }
    }

    override fun save(city: City): City {
        map[city.id] = city
        return city
    }

    override fun deleteAll() {
        map.clear()
    }

    override fun findAll(pageable: Pageable): Page<City> {
        val allCities: List<City> = map.values.toList()
        return PageImpl(allCities, pageable, allCities.size.toLong())
    }

    override fun findById(cityId: String): Optional<City> {
        return Optional.ofNullable(map[cityId])
    }
}