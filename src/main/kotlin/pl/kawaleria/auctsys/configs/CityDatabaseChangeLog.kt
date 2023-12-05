package pl.kawaleria.auctsys.configs

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import io.changock.migration.api.annotations.NonLockGuarded
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource
import pl.kawaleria.auctsys.auctions.domain.City
import pl.kawaleria.auctsys.auctions.domain.CityRepository

@Profile("dev")
@ChangeLog(order = "003")
class CityDatabaseChangeLog {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @ChangeSet(order = "001", id = "insertCities", author = "filip-kaminski")
    fun insertCities(cityRepository: CityRepository, @NonLockGuarded objectMapper: ObjectMapper) {
        if (cityRepository.count() > 1) {
            logger.info("Found cities in db. Omitting import operation")
            return
        }
        val cityImportFilepath = "city_data.json"
        val resource = ClassPathResource(cityImportFilepath)
        val cities: List<City> = objectMapper.readValue(resource.inputStream)
        cityRepository.saveAll(cities.toMutableList())
    }

}