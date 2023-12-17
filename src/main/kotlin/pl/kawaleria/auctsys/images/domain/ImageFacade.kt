package pl.kawaleria.auctsys.images.domain

import net.coobird.thumbnailator.Thumbnails
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.web.multipart.MultipartFile
import pl.kawaleria.auctsys.auctions.domain.AuctionFacade
import pl.kawaleria.auctsys.images.dto.exceptions.ImageDoesNotExistsException
import pl.kawaleria.auctsys.images.dto.responses.AuctionImagesResponse
import pl.kawaleria.auctsys.images.dto.responses.ImageSimplifiedResponse
import pl.kawaleria.auctsys.images.dto.responses.toSimplifiedResponse
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO


open class ImageFacade(
    private val imageRepository: ImageRepository,
    private val thumbnailRules: ThumbnailRules,
    private val imageVerificationRules: ImageVerificationRules,
    private val auctionFacade: AuctionFacade,
    private val imageValidator: ImageValidator,
    private val imageVerificationRequestSender: ImageVerificationRequestSender
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

        return if (imageVerificationRules.enabled) {
            processImagesWithVerification(auctionId, files)
        } else {
            processImagesWithoutVerification(auctionId, files)
        }
    }

    private fun processImagesWithVerification(auctionId: String, files: List<MultipartFile>): List<ImageSimplifiedResponse> {
        logger.debug("Sending images to verification for auction of id $auctionId")

        val images = saveImagesWithVerificationStatus(auctionId, files, ImageVerificationStatus.PENDING)
        imageVerificationRequestSender.sendToVerification(auctionId, VerifyImagesRequestEvent(files, auctionId))

        return images.map { it.toSimplifiedResponse() }
    }

    private fun processImagesWithoutVerification(auctionId: String, files: List<MultipartFile>): List<ImageSimplifiedResponse> {
        logger.debug("Image verification switched off and omitted for auction of id $auctionId")

        val images = saveImagesWithVerificationStatus(auctionId, files, ImageVerificationStatus.VERIFICATION_OMITTED)
        addThumbnailToAuction(auctionId, files.first())
        auctionFacade.accept(auctionId)

        return images.map { it.toSimplifiedResponse() }
    }

    private fun addThumbnailToAuction(auctionId: String, image: MultipartFile): Unit =
        auctionFacade.saveThumbnail(auctionId, prepareThumbnail(image))

    private fun prepareThumbnail(image: MultipartFile): ByteArray {
        val originalImage: BufferedImage = ImageIO.read(image.inputStream)

        val thumbnailWidth: Int = thumbnailRules.width
        val thumbnailHeight: Int = thumbnailRules.height

        val outputStream = ByteArrayOutputStream()

        Thumbnails.of(originalImage)
            .size(thumbnailWidth, thumbnailHeight)
            .outputFormat("png")
            .toOutputStream(outputStream)

        return outputStream.toByteArray()
    }

    private fun saveImagesWithVerificationStatus(auctionId: String, images: List<MultipartFile>,
                                                 verificationStatus: ImageVerificationStatus): List<Image> {
        return images.map { file ->
            file.toImage(auctionId, verificationStatus)
                .also { imageRepository.save(it) }
        }
    }

    fun delete(imageId: String): Unit = imageRepository.delete(findImageById(imageId))

    private fun MultipartFile.toImage(auctionId: String, verificationStatus: ImageVerificationStatus): Image {
        return Image(
            type = this.contentType.toString(),
            size = this.size,
            binaryData = this.bytes,
            auctionId = auctionId,
            verificationStatus = verificationStatus
        )
    }

}

typealias ThumbnailAdder = (String, MultipartFile) -> Unit