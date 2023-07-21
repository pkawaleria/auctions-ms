package pl.kawaleria.auctsys.responses

import org.springframework.data.domain.Page
import pl.kawaleria.auctsys.models.Auction

data class PagedAuctions(
        val auctions: List<AuctionSimplifiedResponse>,
        val pageNumber: Int,
        val pageCount: Int
)

fun Page<Auction>.toPagedAuctions(): PagedAuctions {
    val auctionSimplifiedList = this.content.map { auction ->
        auction.toSimplifiedResponse()
    }
    return PagedAuctions(
            auctions = auctionSimplifiedList,
            pageNumber = this.number,
            pageCount = this.totalPages
    )
}
