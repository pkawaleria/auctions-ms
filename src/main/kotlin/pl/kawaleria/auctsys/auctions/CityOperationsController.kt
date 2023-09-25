package pl.kawaleria.auctsys.auctions

import org.springframework.data.domain.PageRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import pl.kawaleria.auctsys.auctions.domain.CityFacade
import pl.kawaleria.auctsys.auctions.dto.requests.CitiesSearchRequest
import pl.kawaleria.auctsys.auctions.dto.responses.PagedCities

@RestController
@RequestMapping("/cities")
class CityOperationsController(private val cityFacade: CityFacade) {

    @PostMapping("/import")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun importCities(): Unit = cityFacade.importCities()

    @DeleteMapping("/clear")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun deleteCities(): Unit = cityFacade.deleteCities()

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