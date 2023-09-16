package pl.kawaleria.auctsys.images.domain

import org.springframework.web.multipart.MultipartFile

data class ImagesVerificationEvent(
        val images: List<MultipartFile>,
        val auctionId: String,
        val addThumbnailFunction: ThumbnailAdder
)

