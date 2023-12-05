package pl.kawaleria.auctsys.auctions.domain

import org.springframework.data.domain.PageRequest
import pl.kawaleria.auctsys.auctions.dto.requests.CitiesSearchRequest
import pl.kawaleria.auctsys.auctions.dto.responses.PagedCities
import pl.kawaleria.auctsys.auctions.dto.responses.toPagedCities

class CityFacade(private val cityRepository: CityRepository) {

    fun searchCities(searchRequest: CitiesSearchRequest, pageRequest: PageRequest): PagedCities {
        val searchCityName: String? = searchRequest.searchCityName

        return when {
            searchCityName != null -> cityRepository.findByNameContainingIgnoreCase(searchCityName, pageRequest)

            else -> cityRepository.findAll(pageRequest)
        }.toPagedCities()
    }

    // for tests
    fun save(city: City): City = cityRepository.save(city)

}