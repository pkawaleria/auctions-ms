package pl.kawaleria.auctsys.images.dto.responses

import pl.kawaleria.auctsys.images.domain.Image

data class ImageDetailedResponse(
        val id: String?,
        val type: String,
        val size: Long,
        val auctionId: String,
        val binaryData: ByteArray
)

fun Image.toImageDetailedResponse(): ImageDetailedResponse = ImageDetailedResponse(id, type, size, auctionId, binaryData)