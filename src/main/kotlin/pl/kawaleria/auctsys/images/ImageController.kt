package pl.kawaleria.auctsys.images

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import pl.kawaleria.auctsys.images.domain.Image
import pl.kawaleria.auctsys.images.domain.ImageFacade
import pl.kawaleria.auctsys.images.dto.responses.*
@RestController
@RequestMapping("/auction-service/auctions")
class ImageController(private val imageFacade: ImageFacade) {

    @PostMapping("/{auctionId}/images", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun addImages(
        @PathVariable auctionId: String,
        @RequestPart files: List<MultipartFile>
    ): List<ImageDetailedResponse> {
        return imageFacade.addImagesToAuction(auctionId, files).map { image -> image.toImageDetailedResponse() }
    }

    @GetMapping("/{auctionId}/images/{imageId}")
    fun getImage(
        @PathVariable auctionId: String,
        @PathVariable imageId: String
    ): ResponseEntity<ByteArray> {
        val image: Image = imageFacade.findImageById(imageId)

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(image.type!!))
            .body(image.binaryData)
    }

    @GetMapping("/{auctionId}/images")
    fun getImages(@PathVariable auctionId: String): ResponseEntity<AuctionImagesResponse> {
        return ResponseEntity.ok(imageFacade.findImagesByAuctionId(auctionId))
    }

    @DeleteMapping("/{auctionId}/images/{imageId}")
    fun deleteImage(
        @PathVariable auctionId: String,
        @PathVariable imageId: String
    ): ResponseEntity<Unit> {
        imageFacade.delete(imageId)

        return ResponseEntity.noContent().build()
    }
}