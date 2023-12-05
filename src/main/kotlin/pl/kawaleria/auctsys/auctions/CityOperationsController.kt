package pl.kawaleria.auctsys.auctions

import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.*
import pl.kawaleria.auctsys.auctions.domain.CityFacade
import pl.kawaleria.auctsys.auctions.dto.requests.CitiesSearchRequest
import pl.kawaleria.auctsys.auctions.dto.responses.PagedCities

@RestController
@RequestMapping("/auction-service/cities")
class CityOperationsController(private val cityFacade: CityFacade) {

    @GetMapping("/search")
    fun searchCities(
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "10") pageSize: Int,
        @RequestParam(required = false) searchCityName: String?,
    ): PagedCities {
        val pageRequest: PageRequest = PageRequest.of(page, pageSize)
        val searchRequest = CitiesSearchRequest(searchCityName)
        return cityFacade.searchCities(searchRequest, pageRequest)
    }
}