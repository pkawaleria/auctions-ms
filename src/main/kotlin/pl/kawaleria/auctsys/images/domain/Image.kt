package pl.kawaleria.auctsys.images.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "images")
data class Image(
        @Id
        var id: String? = null,
        var type: String,
        var size: Long,
        var binaryData: ByteArray = byteArrayOf(),
        @Indexed
        var auctionId: String
)