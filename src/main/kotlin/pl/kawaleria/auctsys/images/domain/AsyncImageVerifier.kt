package pl.kawaleria.auctsys.images.domain

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import pl.kawaleria.auctsys.auctions.domain.AuctionFacade
import pl.kawaleria.auctsys.images.infrastructure.ContentVerificationClient

@Component
class AsyncImageVerifier(
        private val contentVerificationClient: ContentVerificationClient,
        private val imageRepository: ImageRepository,
        private val auctionFacade: AuctionFacade
) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @EventListener
    @Async
    fun handle(imagesVerificationEvent: ImagesVerificationEvent) {
        val files = imagesVerificationEvent.images
        val auctionId = imagesVerificationEvent.auctionId
        val addThumbnailFunction = imagesVerificationEvent.addThumbnailFunction

        try {
            val inappropriateImage = files.find { contentVerificationClient.verifyImage(it.resource).isInappropriate }

            if (inappropriateImage != null) {
                logger.info("Found explicit image, deleting all images for auction of id $auctionId")
                imageRepository.deleteAllByAuctionId(auctionId)
                auctionFacade.reject(auctionId)
                // TODO: this should be rejectImages and new statuses(also status transitions) should be added ie.
                //  IMAGE_VERIFICATION_PENDING, IMAGE_VERIFICATION_FAILED, INAPPROPRIATE_IMAGE_CONTENT, IMAGES_VERIFIED_SUCCESSFULLY
            } else {
                logger.info("All auction images verified positively for auction id $auctionId")
                addThumbnailFunction(auctionId, files[0])
                auctionFacade.accept(auctionId)
            }
        } catch (e: Exception) {
            logger.error("Error during image verification", e)
            // Here should be some re-check logic, or setting auction status to -manual-check-required-
        }
    }
}

