package pl.kawaleria.auctsys.auctions.dto.responses

import pl.kawaleria.auctsys.auctions.domain.City

data class CitySimplifiedResponse(
        val id: String,
        val name: String,
        val province: String,
        val district: String,
        val commune: String
)

fun City.toSimplifiedResponse(): CitySimplifiedResponse = CitySimplifiedResponse(id, name, province, district, commune)