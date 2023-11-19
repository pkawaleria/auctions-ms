package pl.kawaleria.auctsys.images.domain

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.kawaleria.auctsys.auctions.domain.AuctionFacade
import pl.kawaleria.auctsys.verifications.ContentVerificationClient

@Configuration
@EnableConfigurationProperties(ThumbnailRules::class, ImageVerificationRules::class)
class ImageConfiguration {
    @Bean
    fun imageService(
        repository: ImageRepository,
        thumbnailRules: ThumbnailRules,
        imageVerificationRules: ImageVerificationRules,
        auctionFacade: AuctionFacade,
        imageVerifier: AsyncImageVerifier,
        imageValidator: ImageValidator,
        applicationEventPublisher: ApplicationEventPublisher
    ): ImageFacade {
        return ImageFacade(repository, thumbnailRules, imageVerificationRules, auctionFacade, imageValidator, applicationEventPublisher)
    }
}