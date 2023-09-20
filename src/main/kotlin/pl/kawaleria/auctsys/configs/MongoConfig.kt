package pl.kawaleria.auctsys.configs

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.convert.*
import org.springframework.data.mongodb.core.mapping.MongoMappingContext

@Configuration
class MongoConfig(private val mongoDatabaseFactory: MongoDatabaseFactory,
                  private val mongoMappingContext: MongoMappingContext) {

    @Bean
    fun mappingMongoConverter(): MappingMongoConverter {
        val dbRefResolver: DbRefResolver = DefaultDbRefResolver(mongoDatabaseFactory)
        val converter = MappingMongoConverter(dbRefResolver, mongoMappingContext)
        converter.setTypeMapper(DefaultMongoTypeMapper(null) as MongoTypeMapper?)

        return converter
    }
}