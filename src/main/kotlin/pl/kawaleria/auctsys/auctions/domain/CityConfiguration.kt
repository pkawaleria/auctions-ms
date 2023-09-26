package pl.kawaleria.auctsys.auctions.domain

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CityConfiguration {

    @Bean
    fun cityFacade(repository: CityRepository,
                   objectMapper: ObjectMapper): CityFacade =

        CityFacade(cityRepository = repository, objectMapper = objectMapper)
}