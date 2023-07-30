package pl.kawaleria.auctsys.images.domain

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.web.multipart.MultipartFile
import pl.kawaleria.auctsys.auctions.domain.AuctionFacade
import pl.kawaleria.auctsys.auctions.dto.responses.ApiException
import pl.kawaleria.auctsys.configs.ThumbnailRules
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
    fun findImageById(id: String): Image = imageRepository.findById(id).orElseThrow { ApiException(400, "Image does not exists") }

    fun findImagesByAuctionId(auctionId: String): AuctionImagesResponse {
        val images: List<Image> = imageRepository.findImagesByAuctionId(auctionId)
        val imageIDs: MutableList<String> = mutableListOf()

        for (image: Image in images) imageIDs.add(image.id!!)

        return AuctionImagesResponse(
            imagesCount = images.size,
            imageIDs = imageIDs
        )
    }

    fun addImagesToAuction(auctionId: String, files: List<MultipartFile>): MutableList<Image> {
        imageValidator.validateMultipartFileList(files)
        saveThumbnailToAuction(auctionId, files[0])
        return saveAndReturnImages(auctionId, files)
    }

    private fun saveThumbnailToAuction(auctionId: String, image: MultipartFile): Unit = auctionFacade.saveThumbnail(auctionId, resizeImageToThumbnailFormat(image))

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
    private fun saveAndReturnImages(auctionId: String, images: List<MultipartFile>): MutableList<Image> {
        val imageList: MutableList<Image> = mutableListOf()

        for (image: MultipartFile in images) {
            val newImage = Image(
                type = image.contentType,
                size = image.size,
                binaryData = image.bytes,
                auctionId = auctionId
            )

            imageList.add(newImage)
            imageRepository.save(newImage)
        }

        return imageList
    }

    fun delete(imageId: String): Unit = imageRepository.delete(findImageById(imageId))
}