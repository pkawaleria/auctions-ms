package pl.kawaleria.auctsys.auctions.domain

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate

@Configuration
class CityConfiguration {

    @Bean
    fun cityFacade(repository: CityRepository,
                   mongoTemplate: MongoTemplate,
                   objectMapper: ObjectMapper): CityFacade =

        CityFacade(cityRepository = repository, mongoTemplate = mongoTemplate, objectMapper = objectMapper)
}