package pl.kawaleria.auctsys.configs

import com.mongodb.client.MongoClient
import com.mongodb.client.model.Indexes
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.convert.*
import org.springframework.data.mongodb.core.mapping.MongoMappingContext


@Configuration
class MongoConfig(private val mongoDatabaseFactory: MongoDatabaseFactory,
                  private val mongoMappingContext: MongoMappingContext,
                  private val mongoClient: MongoClient) {

    @Bean
    fun mappingMongoConverter(): MappingMongoConverter {
        val dbRefResolver: DbRefResolver = DefaultDbRefResolver(mongoDatabaseFactory)
        val converter = MappingMongoConverter(dbRefResolver, mongoMappingContext)
        converter.setTypeMapper(DefaultMongoTypeMapper(null) as MongoTypeMapper?)

        return converter
    }

    @Bean
    fun init() {
        val db = mongoClient.getDatabase("auctions-ms-db")
        val auctions = db.getCollection("auctions")
        auctions.createIndex(Indexes.geo2dsphere("location"))
    }
}