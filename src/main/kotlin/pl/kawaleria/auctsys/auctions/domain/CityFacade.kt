package pl.kawaleria.auctsys.auctions.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.data.domain.PageRequest
import pl.kawaleria.auctsys.auctions.dto.exceptions.CanNotDeleteCitiesCollectionException
import pl.kawaleria.auctsys.auctions.dto.exceptions.CanNotImportCitiesException
import pl.kawaleria.auctsys.auctions.dto.requests.CitiesSearchRequest
import pl.kawaleria.auctsys.auctions.dto.responses.PagedCities
import pl.kawaleria.auctsys.auctions.dto.responses.toPagedCities

class CityFacade(private val cityRepository: CityRepository,
                 private val objectMapper: ObjectMapper) {


    @Value("\${files.import.cities.path}")
    private val cityImportFilepath: String = ""

    fun importCities() {
        if (cityRepository.count() != 0L) throw CanNotImportCitiesException()

        val resource = ClassPathResource(cityImportFilepath)
        val cities: List<City> = objectMapper.readValue(resource.inputStream)

        cityRepository.saveAll(cities.toMutableList())
    }

    fun searchCities(searchRequest: CitiesSearchRequest, pageRequest: PageRequest): PagedCities {
        val searchCityName: String? = searchRequest.searchCityName

        return when {
            searchCityName != null -> cityRepository.findByNameContainingIgnoreCase(searchCityName, pageRequest)

            else -> cityRepository.findAll(pageRequest)
        }.toPagedCities()
    }

    fun deleteCities() {
        if (cityRepository.count() <= 0) throw CanNotDeleteCitiesCollectionException()

        cityRepository.deleteAll()
    }

    // for tests
    fun save(city: City): City = cityRepository.save(city)

}