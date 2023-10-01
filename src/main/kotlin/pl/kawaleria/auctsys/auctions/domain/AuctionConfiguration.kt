package pl.kawaleria.auctsys.auctions.domain

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate
import pl.kawaleria.auctsys.categories.domain.CategoryFacade
import pl.kawaleria.auctsys.configs.SecurityHelper
import pl.kawaleria.auctsys.verifications.ContentVerificationClient
import pl.kawaleria.auctsys.verifications.InMemoryContentVerificationClient
import java.time.Clock

@Configuration
@EnableConfigurationProperties(AuctionCreationRules::class, AuctionSearchingRules::class, AuctionVerificationRules::class)
class AuctionConfiguration {

    @Bean
    fun clock(): Clock = Clock.systemDefaultZone()

    @Bean
    fun auctionFacade(
        repository: MongoAuctionRepository,
        cityRepository: CityRepository,
        auctionCreationRules: AuctionCreationRules,
        auctionVerificationRules: AuctionVerificationRules,
        auctionSearchingRules: AuctionSearchingRules,
        clock: Clock,
        categoryFacade: CategoryFacade,
        contentVerificationClient: ContentVerificationClient,
        securityHelper: SecurityHelper,
        mongoTemplate: MongoTemplate
    ): AuctionFacade =

        AuctionFacade(
            auctionRepository = repository,
            cityRepository = cityRepository,
            auctionCreationRules = auctionCreationRules,
            auctionSearchingRules = auctionSearchingRules,
            auctionVerificationRules = auctionVerificationRules,
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
            auctionCreationRules = AuctionCreationRules(days = 10),
            auctionSearchingRules = AuctionSearchingRules(min = 1.0, max = 50.0),
            auctionVerificationRules = AuctionVerificationRules(enabled = true),
            clock = Clock.systemUTC(),
            auctionCategoryDeleter = AuctionCategoryDeleter(auctionRepository),
            categoryFacade = categoryFacade,
            contentVerificationClient = InMemoryContentVerificationClient(),
            securityHelper = SecurityHelper(),
            auctionSearchRepository = auctionRepository
        )
    }
}