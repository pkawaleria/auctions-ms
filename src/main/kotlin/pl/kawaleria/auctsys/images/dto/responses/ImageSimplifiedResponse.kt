package pl.kawaleria.auctsys.images.dto.responses

import pl.kawaleria.auctsys.images.domain.Image

data class ImageSimplifiedResponse(
        val id: String?,
        val type: String,
        val size: Long,
        val auctionId: String,
)

fun Image.toSimplifiedResponse(): ImageSimplifiedResponse = ImageSimplifiedResponse(id, type, size, auctionId)