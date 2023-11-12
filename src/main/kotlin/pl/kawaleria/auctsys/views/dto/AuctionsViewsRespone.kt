package pl.kawaleria.auctsys.views.dto

data class AuctionsViewsRespone(
    val auctionsViewsPerId: Map<String, Long>
) {

    fun getViewsOfAuction(auctionId: String): Long {
        return auctionsViewsPerId[auctionId] ?: 0
    }

}
