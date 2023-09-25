package pl.kawaleria.auctsys.auctions.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "cities")
@JsonIgnoreProperties("Id")
data class City(
        @Id
        val id: String = ObjectId().toString(),

        @JsonProperty("Name")
        val name: String,

        @JsonProperty("Type")
        val type: String,

        @JsonProperty("Province")
        val province: String,

        @JsonProperty("District")
        val district: String,

        @JsonProperty("Commune")
        val commune: String,

        @JsonProperty("Latitude")
        val latitude: Double,

        @JsonProperty("Longitude")
        val longitude: Double
)