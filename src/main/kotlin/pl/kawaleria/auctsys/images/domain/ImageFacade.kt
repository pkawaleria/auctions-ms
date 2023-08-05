package pl.kawaleria.auctsys.images.domain

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.web.multipart.MultipartFile
import pl.kawaleria.auctsys.auctions.domain.AuctionFacade
import pl.kawaleria.auctsys.configs.ThumbnailRules
import pl.kawaleria.auctsys.images.dto.exceptions.ImageDoesNotExistsException
import pl.kawaleria.auctsys.images.dto.responses.AuctionImagesResponse
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

@EnableConfigurationProperties(ThumbnailRules::class)
class ImageFacade(
        private val imageRepository: ImageRepository,
        private val thumbnailRules: ThumbnailRules,
        private val auctionFacade: AuctionFacade,
        private val imageValidator: ImageValidator) {
    fun findImageById(id: String): Image = imageRepository.findById(id).orElseThrow { ImageDoesNotExistsException() }

    fun findImagesByAuctionId(auctionId: String): AuctionImagesResponse {
        val images: List<Image> = imageRepository.findImagesByAuctionId(auctionId)
        val imageIDs = images.mapNotNull { it.id }
        return AuctionImagesResponse(
                imagesCount = images.size,
                imageIDs = imageIDs
        )
    }

    fun addImagesToAuction(auctionId: String, files: List<MultipartFile>): List<Image> {
        imageValidator.validateMultipartFiles(files)
        addThumbnailToAuction(auctionId, files[0])
        return saveImages(auctionId, files)
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
        return images.map { file -> file.toImage(auctionId)
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