package pl.kawaleria.auctsys.auctions.domain

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.geo.GeoJsonPoint
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import pl.kawaleria.auctsys.auctions.dto.exceptions.ExpiredAuctionException
import pl.kawaleria.auctsys.auctions.dto.exceptions.InvalidAuctionCategoryPathException
import java.time.Instant

@Document(collection = "auctions")
data class Auction(
    @Id
    var id: String = ObjectId().toString(),
    var name: String,
    var description: String,
    var price: Double,
    @Indexed
    var auctioneerId: String,
    var thumbnail: ByteArray = byteArrayOf(),
    var category: Category,
    var categoryPath: CategoryPath,
    var productCondition: Condition,
    @Indexed
    var cityId: String,
    var cityName: String,
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    var location: GeoJsonPoint,
    var province: String,
    var status: AuctionStatus = AuctionStatus.NEW,
    var expiresAt: Instant,
    var phoneNumber: String
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

    private fun isExpired(now: Instant): Boolean = expiresAt.isBefore(now)
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
    fun isActive(now: Instant): Boolean = !this.isExpired(now) && this.isAccepted()
}
