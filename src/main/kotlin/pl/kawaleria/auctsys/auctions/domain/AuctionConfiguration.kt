package pl.kawaleria.auctsys.auctions.domain

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.query.FluentQuery
import pl.kawaleria.auctsys.categories.domain.CategoryFacade
import pl.kawaleria.auctsys.configs.SecurityHelper
import pl.kawaleria.auctsys.verifications.ContentVerificationClient
import pl.kawaleria.auctsys.verifications.TextRequest
import pl.kawaleria.auctsys.verifications.VerificationResult
import java.time.Clock
import java.util.*
import java.util.function.Function

@Configuration
@EnableConfigurationProperties(AuctionRules::class, RadiusRules::class)
class AuctionConfiguration {

    @Bean
    fun clock(): Clock = Clock.systemDefaultZone()

    @Bean
    fun auctionFacade(repository: MongoAuctionRepository,
                      cityRepository: CityRepository,
                      auctionRules: AuctionRules,
                      radiusRules: RadiusRules,
                      clock: Clock,
                      categoryFacade: CategoryFacade,
                      contentVerificationClient: ContentVerificationClient,
                      securityHelper: SecurityHelper): AuctionFacade =

            AuctionFacade(
                    auctionRepository = repository,
                    cityRepository = cityRepository,
                    auctionRules = auctionRules,
                    radiusRules = radiusRules,
                    clock = clock,
                    auctionCategoryDeleter = AuctionCategoryDeleter(repository),
                    categoryFacade = categoryFacade,
                    contentVerificationClient = contentVerificationClient,
                    securityHelper = securityHelper
            )

    fun auctionFacadeWithInMemoryRepo(categoryFacade: CategoryFacade): AuctionFacade {

        val auctionRepository = InMemoryAuctionRepository()

        return AuctionFacade(
                auctionRepository = auctionRepository,
                cityRepository = TestCityRepository(),
                auctionRules = AuctionRules(days = 10),
                radiusRules = RadiusRules(min = 1.0, max = 50.0),
                clock = Clock.systemUTC(),
                auctionCategoryDeleter = AuctionCategoryDeleter(auctionRepository),
                categoryFacade = categoryFacade,
                contentVerificationClient = TestContentVerificationClient(),
                securityHelper = SecurityHelper()
        )
    }


    internal class TestCityRepository: CityRepository {
        override fun findByNameContainingIgnoreCase(searchCityName: String, pageRequest: Pageable): Page<City> {
            TODO("Not yet implemented")
        }

        override fun <S : City?> save(entity: S): S {
            TODO("Not yet implemented")
        }

        override fun <S : City?> saveAll(entities: MutableIterable<S>): MutableList<S> {
            TODO("Not yet implemented")
        }

        override fun findById(id: String): Optional<City> {
            TODO("Not yet implemented")
        }

        override fun existsById(id: String): Boolean {
            TODO("Not yet implemented")
        }

        override fun <S : City?> findAll(example: Example<S>): MutableList<S> {
            TODO("Not yet implemented")
        }

        override fun <S : City?> findAll(example: Example<S>, sort: Sort): MutableList<S> {
            TODO("Not yet implemented")
        }

        override fun findAll(): MutableList<City> {
            TODO("Not yet implemented")
        }

        override fun findAll(sort: Sort): MutableList<City> {
            TODO("Not yet implemented")
        }

        override fun findAll(pageable: Pageable): Page<City> {
            TODO("Not yet implemented")
        }

        override fun <S : City?> findAll(example: Example<S>, pageable: Pageable): Page<S> {
            TODO("Not yet implemented")
        }

        override fun findAllById(ids: MutableIterable<String>): MutableList<City> {
            TODO("Not yet implemented")
        }

        override fun count(): Long {
            TODO("Not yet implemented")
        }

        override fun <S : City?> count(example: Example<S>): Long {
            TODO("Not yet implemented")
        }

        override fun deleteById(id: String) {
            TODO("Not yet implemented")
        }

        override fun delete(entity: City) {
            TODO("Not yet implemented")
        }

        override fun deleteAllById(ids: MutableIterable<String>) {
            TODO("Not yet implemented")
        }

        override fun deleteAll(entities: MutableIterable<City>) {
            TODO("Not yet implemented")
        }

        override fun deleteAll() {
            TODO("Not yet implemented")
        }

        override fun <S : City?> findOne(example: Example<S>): Optional<S> {
            TODO("Not yet implemented")
        }

        override fun <S : City?> exists(example: Example<S>): Boolean {
            TODO("Not yet implemented")
        }

        override fun <S : City?, R : Any?> findBy(
            example: Example<S>,
            queryFunction: Function<FluentQuery.FetchableFluentQuery<S>, R>
        ): R {
            TODO("Not yet implemented")
        }

        override fun <S : City?> insert(entity: S): S {
            TODO("Not yet implemented")
        }

        override fun <S : City?> insert(entities: MutableIterable<S>): MutableList<S> {
            TODO("Not yet implemented")
        }

    }
    internal class TestContentVerificationClient : ContentVerificationClient {
        override fun verifyImage(image: Resource): VerificationResult {
            return VerificationResult(false)
        }

        override fun verifyText(text: TextRequest): VerificationResult {
            return VerificationResult(false)
        }

    }
}