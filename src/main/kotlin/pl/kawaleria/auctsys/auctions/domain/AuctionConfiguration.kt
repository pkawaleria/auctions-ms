package pl.kawaleria.auctsys.auctions.domain

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.kawaleria.auctsys.categories.domain.CategoryFacade
import java.time.Clock

@Configuration
@EnableConfigurationProperties(AuctionRules::class)
class AuctionConfiguration {

    @Bean
    fun clock(): Clock {
        return Clock.systemDefaultZone()
    }

    @Bean
    fun auctionFacade(repository: MongoAuctionRepository,
                      auctionRules: AuctionRules,
                      clock: Clock,
                      categoryFacade: CategoryFacade): AuctionFacade =

            AuctionFacade(auctionRepository = repository,
                    auctionRules = auctionRules,
                    clock = clock,
                    auctionCategoryDeleter = AuctionCategoryDeleter(repository),
                    categoryFacade = categoryFacade
            )

    fun auctionFacadeWithInMemoryRepo(categoryFacade: CategoryFacade): AuctionFacade {
        val auctionRepository = InMemoryAuctionRepository()
        return AuctionFacade(
                auctionRepository = auctionRepository,
                auctionRules = AuctionRules(days = 10),
                clock = Clock.systemUTC(),
                auctionCategoryDeleter = AuctionCategoryDeleter(auctionRepository),
                categoryFacade = categoryFacade
        )
    }
}