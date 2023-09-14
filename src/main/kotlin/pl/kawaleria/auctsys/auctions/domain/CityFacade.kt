package pl.kawaleria.auctsys.auctions.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.core.io.ClassPathResource
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.ResponseEntity
import pl.kawaleria.auctsys.auctions.dto.requests.CitiesSearchRequest
import pl.kawaleria.auctsys.auctions.dto.responses.PagedCities
import pl.kawaleria.auctsys.auctions.dto.responses.toPagedCities

class CityFacade(private val cityRepository: CityRepository,
private val mongoTemplate: MongoTemplate) {

    fun importCities(): ResponseEntity<String> {
        val objectMapper: ObjectMapper = jacksonObjectMapper()
        val resource = ClassPathResource("city_data.json")
        val cities: List<City> = objectMapper.readValue(resource.inputStream)

        if (cityRepository.count() == 0L) {
            cityRepository.saveAll(cities)
            return ResponseEntity.ok().body("Successfully added cities")
        }

        return ResponseEntity.badRequest().body("Cities document is not empty")
    }

    fun deleteCities(): ResponseEntity<String> {
        if (cityRepository.count() > 0) {
            cityRepository.deleteAll()
            mongoTemplate.dropCollection("cities")
            return ResponseEntity.ok().body("Successfully deleted cities")
        }

        return ResponseEntity.badRequest().body("Cities document is empty")
    }

    fun searchCities(searchRequest: CitiesSearchRequest, pageRequest: PageRequest): PagedCities {
        val searchCityName: String? = searchRequest.searchCityName

        return when {
            searchCityName != null -> cityRepository.findByNameContainingIgnoreCase(searchCityName, pageRequest)

            else -> cityRepository.findAll(pageRequest)
        }.toPagedCities()
    }
}