package pl.kawaleria.auctsys.images.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "images")
class Image(
    @Id
    var id: String? = null,
    var type: String? = null,
    var size: Long? = null,
    var binaryData: ByteArray? = null,
    var auctionId: String? = null
)