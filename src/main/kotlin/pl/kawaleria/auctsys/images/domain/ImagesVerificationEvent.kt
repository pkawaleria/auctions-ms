package pl.kawaleria.auctsys.images.domain

import org.springframework.web.multipart.MultipartFile
import java.time.Instant

data class ImagesVerificationEvent(
        val images: List<MultipartFile>,
        val auctionId: String,
        val addThumbnailFunction: ThumbnailAdder
)


data class VerifyImagesRequestEvent(
        val images: List<MultipartFile>,
        val auctionId: String,
        val timestamp: Instant = Instant.now(),
)
