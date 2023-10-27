package pl.kawaleria.auctsys.configs

import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import pl.kawaleria.auctsys.auctions.domain.City
import pl.kawaleria.auctsys.auctions.domain.CityRepository

@ChangeLog(order = "003")
class CityDatabaseChangeLog {

    @ChangeSet(order = "001", id = "insertCities", author = "filip-kaminski")
    fun insertCities(cityRepository: CityRepository) {
        val cities: MutableList<City> = thereAreCities()
        cityRepository.saveAll(cities)
    }

    private fun thereAreCities(): MutableList<City> {
        return mutableListOf(
            City(
                name = "Abramowice Kościelne",
                type = "village",
                province = "lubelskie",
                district = "lubelski",
                commune = "Głusk-gmina wiejska",
                latitude = 51.1914,
                longitude = 22.6294
            ),
            City(
                name = "Abramowice Prywatne",
                type = "village",
                province = "lubelskie",
                district = "lubelski",
                commune = "Głusk-gmina wiejska",
                latitude = 51.2047,
                longitude = 22.6206
            ),
            City(
                name = "Abramów",
                type = "village",
                province = "lubelskie",
                district = "lubartowski",
                commune = "Abramów-gmina wiejska",
                latitude = 51.4561,
                longitude = 22.3158
            )
        )
    }

}