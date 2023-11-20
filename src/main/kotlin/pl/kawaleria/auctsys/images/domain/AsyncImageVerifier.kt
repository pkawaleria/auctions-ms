package pl.kawaleria.auctsys.images.domain

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import pl.kawaleria.auctsys.auctions.domain.AuctionFacade
import pl.kawaleria.auctsys.verifications.ContentVerificationClient

@Component
class AsyncImageVerifier(private val contentVerificationClient: ContentVerificationClient,
                         private val imageRepository: ImageRepository,
                         private val auctionFacade: AuctionFacade) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    @EventListener
    @Async
    fun handle(imagesVerificationEvent: ImagesVerificationEvent) {
        val files: List<MultipartFile> = imagesVerificationEvent.images
        val auctionId: String = imagesVerificationEvent.auctionId
        val addThumbnailFunction: ThumbnailAdder = imagesVerificationEvent.addThumbnailFunction

        var foundInappropriateImage = false

        try {
            foundInappropriateImage = files.any { contentVerificationClient.verifyImage(it.resource).isInappropriate }
        } catch (e: Exception) {
            logger.error("Error during image verification", e)
            //TODO: Here should be some re-check logic, or setting auction status to -manual-check-required-
        }

        if (foundInappropriateImage) {
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
    }
}

