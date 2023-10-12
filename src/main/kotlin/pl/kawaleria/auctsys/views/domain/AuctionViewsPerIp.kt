package pl.kawaleria.auctsys.views.domain

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.index.Indexed
import java.time.Instant

// ttl set to 14 days, that means after 14 days from entry creation in redis it will be automatically deleted
@RedisHash(value = "AuctionViewsPerIp", timeToLive = 14 * 24 * 60 * 60)
data class AuctionViewsPerIp(
    @Id
    val id: String,
    val auctionId: String,
    @Indexed
    val ipAddress: String,
    var lastViewed: Instant,
    val viewedTimestamps: MutableList<Instant> = mutableListOf(),
    var viewCounter: Int = 0
) {
    constructor(key: AuctionViewPerIpKey, viewedInstant: Instant) : this(
        id = key.formatToStringRepresentation(),
        auctionId = key.auctionId,
        ipAddress = key.ipAddress,
        lastViewed = viewedInstant,
        viewedTimestamps = mutableListOf(),
        viewCounter = 0
    )

    fun recordNewView(viewedInstant: Instant): AuctionViewsPerIp {
        this.viewCounter++
        this.lastViewed = viewedInstant
        this.viewedTimestamps.add(viewedInstant)
        return this
    }

    fun isFirstView(): Boolean = this.viewCounter == 0
}

data class AuctionViewPerIpKey(
    val ipAddress: String,
    val auctionId: String
) {
    fun formatToStringRepresentation(): String = "${auctionId}:${ipAddress}"
}