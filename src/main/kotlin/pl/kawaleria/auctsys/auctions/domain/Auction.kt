package pl.kawaleria.auctsys.auctions.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed
import org.springframework.data.mongodb.core.mapping.Document
import pl.kawaleria.auctsys.auctions.dto.exceptions.ExpiredAuctionException
import pl.kawaleria.auctsys.auctions.dto.exceptions.InvalidAuctionCategoryPathException
import java.time.Instant

@Document(collection = "auctions")
data class Auction(
    @Id
    var id: String? = null,
    var name: String? = null,
    var description: String? = null,
    var price: Double? = null,
    var auctioneerId: String? = null,
    var thumbnail: ByteArray? = null,
    var category: Category,
    var categoryPath: CategoryPath,
    var productCondition: Condition,
    var cityId: String,
    var cityName: String,
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    var location: GeoJsonPoint,
    var status: AuctionStatus = AuctionStatus.NEW,
    var expiresAt: Instant
) {

    fun assignPath(categoryPath: CategoryPath) {
        if (categoryPath.pathElements.isEmpty()) throw InvalidAuctionCategoryPathException()

        this.categoryPath = categoryPath
        this.category = categoryPath.lastCategory()
    }

    fun accept() {
        if (isExpired()) throw ExpiredAuctionException()

        updateStatus(status.accept(auction = this))
    }

    fun reject() {
        if (isExpired()) throw ExpiredAuctionException()

        updateStatus(status.reject(auction = this))
    }

    fun archive(): Unit = updateStatus(status.archive(auction = this))

    private fun isExpired(): Boolean = expiresAt.isBefore(Instant.now())

    private fun updateStatus(status: AuctionStatus) {
        this.status = status
    }

    fun dropCategoryFromPath(categoryName: String) {
        if (isCurrentCategory(categoryName)) {
            categoryPath.removeLast()
            category = categoryPath.lastCategory()
        } else { // is just path element
            categoryPath.remove(categoryName)
        }
    }

    private fun isCurrentCategory(categoryId: String): Boolean = categoryId == category.id

    fun isAccepted(): Boolean = this.status == AuctionStatus.ACCEPTED
    fun isRejected(): Boolean = this.status == AuctionStatus.REJECTED
    fun isArchived(): Boolean = this.status == AuctionStatus.ARCHIVED
}
