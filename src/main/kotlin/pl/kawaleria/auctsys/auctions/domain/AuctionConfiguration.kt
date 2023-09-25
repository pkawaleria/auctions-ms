package pl.kawaleria.auctsys.auctions.domain

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate
import pl.kawaleria.auctsys.categories.domain.CategoryFacade
import pl.kawaleria.auctsys.citys.domain.InMemoryCityRepository
import pl.kawaleria.auctsys.configs.SecurityHelper
import pl.kawaleria.auctsys.verifications.ContentVerificationClient
import pl.kawaleria.auctsys.verifications.InMemoryContentVerificationClient
import java.time.Clock

@Configuration
@EnableConfigurationProperties(AuctionRules::class, RadiusRules::class)
class AuctionConfiguration {

    @Bean
    fun clock(): Clock = Clock.systemDefaultZone()

    @Bean
    fun auctionFacade(
        repository: MongoAuctionRepository,
        cityRepository: CityRepository,
        auctionRules: AuctionRules,
        radiusRules: RadiusRules,
        clock: Clock,
        categoryFacade: CategoryFacade,
        contentVerificationClient: ContentVerificationClient,
        securityHelper: SecurityHelper,
        mongoTemplate: MongoTemplate
    ): AuctionFacade =

        AuctionFacade(
            auctionRepository = repository,
            cityRepository = cityRepository,
            auctionRules = auctionRules,
            radiusRules = radiusRules,
            clock = clock,
            auctionCategoryDeleter = AuctionCategoryDeleter(repository),
            categoryFacade = categoryFacade,
            contentVerificationClient = contentVerificationClient,
            securityHelper = securityHelper,
            auctionSearchRepository = MongoAuctionSearchRepository(mongoTemplate)
        )

    fun auctionFacadeWithInMemoryRepo(categoryFacade: CategoryFacade): AuctionFacade {

        val auctionRepository = InMemoryAuctionRepository()

        return AuctionFacade(
            auctionRepository = auctionRepository,
            cityRepository = InMemoryCityRepository(),
            auctionRules = AuctionRules(days = 10),
            radiusRules = RadiusRules(min = 1.0, max = 50.0),
            clock = Clock.systemUTC(),
            auctionCategoryDeleter = AuctionCategoryDeleter(auctionRepository),
            categoryFacade = categoryFacade,
            contentVerificationClient = InMemoryContentVerificationClient(),
            securityHelper = SecurityHelper(),
            auctionSearchRepository = auctionRepository
        )
    }
}