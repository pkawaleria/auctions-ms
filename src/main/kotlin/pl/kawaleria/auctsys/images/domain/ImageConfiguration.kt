package pl.kawaleria.auctsys.images.domain

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.kawaleria.auctsys.auctions.domain.AuctionFacade
import pl.kawaleria.auctsys.verifications.ContentVerificationClient

@Configuration
@EnableConfigurationProperties(ThumbnailRules::class)
class ImageConfiguration {
    @Bean
    fun imageService(
        repository: ImageRepository,
        thumbnailRules: ThumbnailRules,
        auctionFacade: AuctionFacade,
        imageVerifier: AsyncImageVerifier,
        imageValidator: ImageValidator,
        contentVerificationClient: ContentVerificationClient,
        applicationEventPublisher: ApplicationEventPublisher
    ): ImageFacade {
        return ImageFacade(repository, thumbnailRules, auctionFacade, imageValidator, applicationEventPublisher, contentVerificationClient)
    }
}