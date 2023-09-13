package pl.kawaleria.auctsys.auctions.domain

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate

@Configuration
class CityConfiguration {

    @Bean
    fun cityFacade(repository: CityRepository,
                   mongoTemplate: MongoTemplate): CityFacade =

        CityFacade(cityRepository = repository, mongoTemplate = mongoTemplate)
}