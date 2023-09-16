package pl.kawaleria.auctsys.auctions.dto.responses

import org.springframework.data.domain.Page
import pl.kawaleria.auctsys.auctions.domain.City

data class PagedCities(
        val cities: List<CitySimplifiedResponse>,
        val pageNumber: Int,
        val pageCount: Int
)

fun Page<City>.toPagedCities(): PagedCities {
    val auctionSimplifiedList: List<CitySimplifiedResponse> = this.content.map { city ->
        city.toSimplifiedResponse()
    }

    return PagedCities(
            cities = auctionSimplifiedList,
            pageNumber = this.number,
            pageCount = this.totalPages
    )
}