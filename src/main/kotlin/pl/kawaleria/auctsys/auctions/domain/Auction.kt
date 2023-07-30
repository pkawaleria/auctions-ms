package pl.kawaleria.auctsys.auctions.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "auctions")
data class Auction(
        @Id
        var id: String? = null,
        var name: String? = null,
        var category: Category? = null,
        var description: String? = null,
        var price: Double? = null,
        var auctioneerId: String? = null,
        var thumbnail: ByteArray? = null
)
