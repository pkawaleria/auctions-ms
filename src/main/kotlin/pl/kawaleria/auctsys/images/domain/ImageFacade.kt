package pl.kawaleria.auctsys.images.domain

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.web.multipart.MultipartFile
import pl.kawaleria.auctsys.auctions.domain.AuctionFacade
import pl.kawaleria.auctsys.images.dto.exceptions.ImageDoesNotExistsException
import pl.kawaleria.auctsys.images.dto.responses.AuctionImagesResponse
import pl.kawaleria.auctsys.images.dto.responses.ImageSimplifiedResponse
import pl.kawaleria.auctsys.images.dto.responses.toSimplifiedResponse
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO


open class ImageFacade(
    private val imageRepository: ImageRepository,
    private val thumbnailRules: ThumbnailRules,
    private val imageVerificationRules: ImageVerificationRules,
    private val auctionFacade: AuctionFacade,
    private val imageValidator: ImageValidator,
    private val eventPublisher: ApplicationEventPublisher,
) {

    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun findImageById(id: String): Image = imageRepository.findById(id).orElseThrow { ImageDoesNotExistsException() }

    fun findImagesByAuctionId(auctionId: String): AuctionImagesResponse {
        val images: List<Image> = imageRepository.findImagesByAuctionId(auctionId)
        val imageIDs: List<String> = images.mapNotNull { it.id }
        return AuctionImagesResponse(
            imagesCount = images.size,
            imageIDs = imageIDs
        )
    }

    fun createImages(auctionId: String, files: List<MultipartFile>): List<ImageSimplifiedResponse> {
        imageValidator.validateMultipartFiles(files)
        val images: List<Image> = saveImages(auctionId, files)
        publishImageVerification(files, auctionId)
        return images.map { image -> image.toSimplifiedResponse() }
    }

    private fun publishImageVerification(
        files: List<MultipartFile>,
        auctionId: String
    ) {
        if (imageVerificationRules.enabled) {
            eventPublisher.publishEvent(ImagesVerificationEvent(files, auctionId, this::addThumbnailToAuction))
        } else {
            addThumbnailToAuction(auctionId, files.first())
            logger.debug("Image verification switched off and omitted for auction of id $auctionId")
        }
    }

    private fun addThumbnailToAuction(auctionId: String, image: MultipartFile): Unit =
        auctionFacade.saveThumbnail(auctionId, resizeImageToThumbnailFormat(image))

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