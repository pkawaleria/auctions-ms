package pl.kawaleria.auctsys.auctions.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import pl.kawaleria.auctsys.auctions.dto.exceptions.ExpiredAuctionException
import java.time.Instant

@Document(collection = "auctions")
data class Auction(
        @Id
        var id: String? = null,
        var name: String? = null,
        var category: Category? = null,
        var description: String? = null,
        var price: Double? = null,
        var auctioneerId: String,
        var status: AuctionStatus = AuctionStatus.NEW,
        var expiresAt: Instant
) {
    fun accept() {
        if (isExpired()) {
            throw ExpiredAuctionException()
        }
        updateStatus(status.accept(auction = this))
    }
    fun reject() {
        if (isExpired()) {
            throw ExpiredAuctionException()
        }
        updateStatus(status.reject(auction = this))
    }
    fun archive() = updateStatus(status.archive(auction = this))

    private fun isExpired() : Boolean = expiresAt.isBefore(Instant.now())

    private fun updateStatus(status: AuctionStatus) {
        this.status = status
    }

    fun isAccepted(): Boolean = this.status == AuctionStatus.ACCEPTED
    fun isRejected(): Boolean = this.status == AuctionStatus.REJECTED
    fun isArchived(): Boolean = this.status == AuctionStatus.ARCHIVED
}
