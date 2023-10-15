package pl.kawaleria.auctsys.views.domain

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash


@RedisHash("AuctionViews")
data class AuctionViews(
    @Id
    val auctionId: String,
    var viewCounter: Long = 0
) {
    fun recordNewView() : AuctionViews {
        this.viewCounter++
        return this
    }
}