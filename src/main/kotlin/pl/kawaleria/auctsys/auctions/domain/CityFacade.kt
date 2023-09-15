package pl.kawaleria.auctsys.auctions.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.core.io.ClassPathResource
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.MongoTemplate
import pl.kawaleria.auctsys.auctions.dto.exceptions.CanNotDeleteCitiesCollectionException
import pl.kawaleria.auctsys.auctions.dto.exceptions.CanNotImportCitiesException
import pl.kawaleria.auctsys.auctions.dto.requests.CitiesSearchRequest
import pl.kawaleria.auctsys.auctions.dto.responses.PagedCities
import pl.kawaleria.auctsys.auctions.dto.responses.toPagedCities

class CityFacade(private val cityRepository: CityRepository,
                 private val mongoTemplate: MongoTemplate,
                 private val objectMapper: ObjectMapper) {

    fun importCities() {
        val resource = ClassPathResource("city_data.json")
        val cities: List<City> = objectMapper.readValue(resource.inputStream)

        if (cityRepository.count() == 0L) {
            cityRepository.saveAll(cities)
        } else throw CanNotImportCitiesException()
    }

    fun deleteCities() {
        if (cityRepository.count() > 0) {
            cityRepository.deleteAll()
            mongoTemplate.dropCollection("cities")
        } else throw CanNotDeleteCitiesCollectionException()
    }

    fun searchCities(searchRequest: CitiesSearchRequest, pageRequest: PageRequest): PagedCities {
        val searchCityName: String? = searchRequest.searchCityName

        return when {
            searchCityName != null -> cityRepository.findByNameContainingIgnoreCase(searchCityName, pageRequest)

            else -> cityRepository.findAll(pageRequest)
        }.toPagedCities()
    }
}