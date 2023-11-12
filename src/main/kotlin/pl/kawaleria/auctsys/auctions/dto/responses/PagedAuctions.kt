package pl.kawaleria.auctsys.auctions.dto.responses

import org.springframework.data.domain.Page
import pl.kawaleria.auctsys.auctions.domain.Auction
import pl.kawaleria.auctsys.views.dto.AuctionsViewsRespone

data class PagedAuctions(
    val auctions: List<AuctionSimplifiedResponse>,
    val pageNumber: Int,
    val pageCount: Int,
    val totalAuctionsCount: Long
)

fun Page<Auction>.toPagedAuctions(): PagedAuctions {
    val auctionSimplifiedList: List<AuctionSimplifiedResponse> = this.content.map { auction ->
        auction.toSimplifiedResponse()
    }

    return PagedAuctions(
        auctions = auctionSimplifiedList,
        pageNumber = this.number,
        pageCount = this.totalPages,
        totalAuctionsCount = this.totalElements
    )
}

fun Page<Auction>.toPagedAuctions(auctionsViews: AuctionsViewsRespone): PagedAuctions {
    val auctionSimplifiedList: List<AuctionSimplifiedResponse> = this.content.map { auction ->
        auction.toSimplifiedResponse(viewCounter = auctionsViews.getViewsOfAuction(auction.id))
    }


    return PagedAuctions(
        auctions = auctionSimplifiedList,
        pageNumber = this.number,
        pageCount = this.totalPages,
        totalAuctionsCount = this.totalElements
    )
}
