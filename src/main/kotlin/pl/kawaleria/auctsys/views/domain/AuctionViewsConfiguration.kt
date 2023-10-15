package pl.kawaleria.auctsys.views.domain

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock


@Configuration
class AuctionViewsConfiguration {

    @Bean
    fun auctionViewsQueryFacade(
        auctionViewsPerIpRepository: RedisAuctionViewsPerIpRepository,
        auctionViewsRepository: RedisAuctionViewsRepository,
    ): AuctionViewsQueryFacade =

        AuctionViewsQueryFacade(
            auctionViewsPerIpRepository = auctionViewsPerIpRepository,
            auctionViewsRepository = auctionViewsRepository,
        )


    @Bean
    fun auctionViewsRecorder(
        auctionViewsPerIpRepository: RedisAuctionViewsPerIpRepository,
        auctionViewsRepository: RedisAuctionViewsRepository,
        clock: Clock
    ): AuctionViewsRecorder =

        AuctionViewsRecorder(
            auctionViewsPerIpRepository = auctionViewsPerIpRepository,
            auctionViewsRepository = auctionViewsRepository,
            clock = clock
        )

    fun auctionViewsModuleWithInMemoryRepositories(clock: Clock): Pair<AuctionViewsQueryFacade, AuctionViewsRecorder> {
        val auctionViewsPerIpRepository = InMemoryAuctionViewsPerIpRepository()
        val auctionViewsRepository = InMemoryAuctionViewsRepository()

        val auctionViewsQueryFacade = AuctionViewsQueryFacade(
            auctionViewsPerIpRepository = auctionViewsPerIpRepository,
            auctionViewsRepository = auctionViewsRepository,
        )
        val auctionViewsRecorder = AuctionViewsRecorder(
            auctionViewsPerIpRepository = auctionViewsPerIpRepository,
            auctionViewsRepository = auctionViewsRepository,
            clock = clock
        )
        return Pair(auctionViewsQueryFacade, auctionViewsRecorder)
    }


    fun auctionViewsQueryFacadeWithInMemoryRepositories(): AuctionViewsQueryFacade =
        AuctionViewsQueryFacade(
            auctionViewsPerIpRepository = InMemoryAuctionViewsPerIpRepository(),
            auctionViewsRepository = InMemoryAuctionViewsRepository(),
        )
}