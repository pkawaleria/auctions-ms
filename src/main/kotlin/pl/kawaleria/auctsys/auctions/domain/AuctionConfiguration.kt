package pl.kawaleria.auctsys.auctions.domain

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.MongoTemplate
import pl.kawaleria.auctsys.auctions.dto.events.AuctionViewedEvent
import pl.kawaleria.auctsys.categories.domain.CategoryFacade
import pl.kawaleria.auctsys.commons.SecurityHelper
import pl.kawaleria.auctsys.verifications.ContentVerificationClient
import pl.kawaleria.auctsys.verifications.InMemoryContentVerificationClient
import pl.kawaleria.auctsys.views.domain.AuctionViewsQueryFacade
import java.time.Clock

@Configuration
@EnableConfigurationProperties(
    AuctionCreationRules::class,
    AuctionSearchingRules::class,
    AuctionVerificationRules::class
)
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
        auctionViewsQueryFacade: AuctionViewsQueryFacade,
        contentVerificationClient: ContentVerificationClient,
        securityHelper: SecurityHelper,
        mongoTemplate: MongoTemplate,
        applicationEventPublisher: ApplicationEventPublisher
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
            auctionSearchRepository = MongoAuctionSearchRepository(mongoTemplate),
            auctionViewsQueryFacade = auctionViewsQueryFacade,
            auctionEventPublisher = SpringAuctionEventPublisher(applicationEventPublisher),
            auctionValidator = AuctionValidator()
        )

    fun auctionFacadeWithInMemoryRepo(
        categoryFacade: CategoryFacade,
        auctionViewsQueryFacade: AuctionViewsQueryFacade
    ): AuctionFacade {
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
            auctionSearchRepository = auctionRepository,
            auctionViewsQueryFacade = auctionViewsQueryFacade,
            auctionEventPublisher = TestAuctionEventPublisher(),
            auctionValidator = AuctionValidator()
        )
    }

    internal class TestAuctionEventPublisher : AuctionDomainEventPublisher {
        private val logger = LoggerFactory.getLogger(this.javaClass)


        override fun publishAuctionView(auctionViewedEvent: AuctionViewedEvent) {
            // we do not need the event to be actually sent since that test config is only for testing the auction facade in isolation
            logger.info("Test auction event publisher received auction viewed event $auctionViewedEvent")
        }

    }
}