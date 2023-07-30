package pl.kawaleria.auctsys.auctions.domain

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AuctionConfiguration {
    @Bean
    fun auctionService(repository: AuctionRepository): AuctionFacade = AuctionFacade(repository)

}