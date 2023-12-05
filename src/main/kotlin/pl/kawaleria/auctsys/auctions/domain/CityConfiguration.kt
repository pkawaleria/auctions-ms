package pl.kawaleria.auctsys.auctions.domain

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CityConfiguration {

    @Bean
    fun cityFacade(repository: CityRepository): CityFacade =

        CityFacade(cityRepository = repository)
}