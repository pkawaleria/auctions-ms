package pl.kawaleria.auctsys.auctions.domain

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
@EnableConfigurationProperties(AuctionRules::class)
class AuctionConfiguration {

    @Bean
    fun clock(): Clock {
        return Clock.systemDefaultZone()
    }


    @Bean
    fun auctionFacade(repository: MongoAuctionRepository, auctionRules: AuctionRules, clock: Clock): AuctionFacade =
            AuctionFacade(auctionRepository = repository, auctionRules = auctionRules, clock = clock)


    fun auctionFacadeWithInMemoryRepo(): AuctionFacade =
            AuctionFacade(InMemoryAuctionRepository(), AuctionRules(10), Clock.systemUTC())


}