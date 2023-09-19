package pl.kawaleria.auctsys.auctions.domain

import org.springframework.data.mongodb.core.index.GeoSpatialIndexed

data class Location(
        val type: String,
        @GeoSpatialIndexed
        val coordinates: List<String>
)