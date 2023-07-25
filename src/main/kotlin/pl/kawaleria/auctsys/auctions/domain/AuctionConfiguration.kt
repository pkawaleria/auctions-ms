package pl.kawaleria.auctsys.auctions.domain

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AuctionConfiguration {
    @Bean
    fun auctionService(repository: AuctionRepository) : AuctionService {
        return AuctionService(repository)
    }

}