package pl.kawaleria.auctsys.images.domain

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ApplicationEventPublisher
import org.springframework.web.multipart.MultipartFile
import pl.kawaleria.auctsys.auctions.domain.AuctionFacade
import pl.kawaleria.auctsys.configs.ThumbnailRules
import pl.kawaleria.auctsys.images.dto.exceptions.ImageDoesNotExistsException
import pl.kawaleria.auctsys.images.dto.responses.AuctionImagesResponse
import pl.kawaleria.auctsys.images.infrastructure.ContentVerificationClient
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

@EnableConfigurationProperties(ThumbnailRules::class)
open class ImageFacade(
        private val imageRepository: ImageRepository,
        private val thumbnailRules: ThumbnailRules,
        private val auctionFacade: AuctionFacade,
        private val imageValidator: ImageValidator,
        private val eventPublisher: ApplicationEventPublisher,
        private val contentVerificationClient: ContentVerificationClient

) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun findImageById(id: String): Image = imageRepository.findById(id).orElseThrow { ImageDoesNotExistsException() }

    fun findImagesByAuctionId(auctionId: String): AuctionImagesResponse {
        val images: List<Image> = imageRepository.findImagesByAuctionId(auctionId)
        val imageIDs = images.mapNotNull { it.id }
        return AuctionImagesResponse(
                imagesCount = images.size,
                imageIDs = imageIDs
        )
    }

    fun createImages(auctionId: String, files: List<MultipartFile>): List<Image> {
        imageValidator.validateMultipartFiles(files)
        val images = saveImages(auctionId, files)

        //TODO: Zadanie dla Filipa - sprawdzic dlaczego w metodzie asynchronicznej z uzyciem event publishera lecą 500
        // z REST serwisu verifiera
        // Cos sie psuje z naszym klientem (klasa z adnotacja @HttpExchange), moze jakies headery requesta sa usuwane/dodawane
        // lub request jest jakos manipulowany
//        eventPublisher.publishEvent(ImagesVerificationEvent(files, auctionId, this::addThumbnailToAuction))
        verifyImages(auctionId, files)
        logger.info("Images added for auction $auctionId, verification is pending")
        return images
    }

    fun verifyImages(auctionId: String, files: List<MultipartFile>) {
        try {
            val foundInappropriateImage = files.any { contentVerificationClient.verifyImage(it.resource).isInappropriate }

            if (foundInappropriateImage) {
                logger.info("Found explicit image, deleting all images for auction of id $auctionId")
                imageRepository.deleteAllByAuctionId(auctionId)
                auctionFacade.reject(auctionId)
            } else {
                logger.info("All auction images verified positively for auction id $auctionId")
                addThumbnailToAuction(auctionId, files[0])
                auctionFacade.accept(auctionId)
            }
        } catch (e: Exception) {
            logger.error("Error during image verification", e)
            // Here should be some re-check logic, or setting auction status to -manual-check-required-
        }
    }

    private fun addThumbnailToAuction(auctionId: String, image: MultipartFile): Unit = auctionFacade.saveThumbnail(auctionId, resizeImageToThumbnailFormat(image))

    private fun resizeImageToThumbnailFormat(image: MultipartFile): ByteArray {
        val originalImage: BufferedImage = ImageIO.read(image.inputStream)

        val scaledImage = BufferedImage(thumbnailRules.width, thumbnailRules.height, BufferedImage.TYPE_INT_RGB)

        val g: Graphics2D = scaledImage.createGraphics()
        g.drawImage(originalImage, 0, 0, thumbnailRules.width, thumbnailRules.height, null)
        g.dispose()

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(scaledImage, "jpg", outputStream)

        return outputStream.toByteArray()
    }

    private fun saveImages(auctionId: String, images: List<MultipartFile>): List<Image> {
        return images.map { file ->
            file.toImage(auctionId)
                    .also { imageRepository.save(it) }
        }
    }

    fun delete(imageId: String): Unit = imageRepository.delete(findImageById(imageId))

    private fun MultipartFile.toImage(auctionId: String): Image {
        return Image(
                type = this.contentType.toString(),
                size = this.size,
                binaryData = this.bytes,
                auctionId = auctionId
        )
    }

}

typealias ThumbnailAdder = (String, MultipartFile) -> Unit