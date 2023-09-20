package pl.kawaleria.auctsys.auctions.domain

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import pl.kawaleria.auctsys.categories.domain.CategoryFacade
import pl.kawaleria.auctsys.configs.SecurityHelper
import pl.kawaleria.auctsys.verifications.ContentVerificationClient
import pl.kawaleria.auctsys.verifications.TextRequest
import pl.kawaleria.auctsys.verifications.VerificationResult
import java.time.Clock

@Configuration
@EnableConfigurationProperties(AuctionRules::class)
class AuctionConfiguration {

    @Bean
    fun clock(): Clock = Clock.systemDefaultZone()

    @Bean
    fun auctionFacade(repository: MongoAuctionRepository,
                      auctionRules: AuctionRules,
                      clock: Clock,
                      categoryFacade: CategoryFacade,
                      contentVerificationClient: ContentVerificationClient,
                      securityHelper: SecurityHelper): AuctionFacade =

            AuctionFacade(
                    auctionRepository = repository,
                    auctionRules = auctionRules,
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
                auctionRules = AuctionRules(days = 10),
                clock = Clock.systemUTC(),
                auctionCategoryDeleter = AuctionCategoryDeleter(auctionRepository),
                categoryFacade = categoryFacade,
                contentVerificationClient = TestContentVerificationClient(),
                securityHelper = SecurityHelper()
        )
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