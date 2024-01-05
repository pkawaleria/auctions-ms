package pl.kawaleria.auctsys.views.domain

import pl.kawaleria.auctsys.views.dto.AuctionViewsFromIpResponse
import pl.kawaleria.auctsys.views.dto.AuctionsViewsRespone

class AuctionViewsQueryFacade(
    private val auctionViewsPerIpRepository: AuctionViewsPerIpRepository,
    private val auctionViewsRepository: AuctionViewsRepository
) {
    fun getAuctionViews(auctionId: String): Long =
        auctionViewsRepository.findById(auctionId).map { it.viewCounter }.orElse(0L)


    fun getAuctionsViews(auctionsIds: List<String>): AuctionsViewsRespone {
        val auctionsViews = auctionsIds
            .mapNotNull { auctionViewsRepository.findById(it).orElse(null) }
            .associate { it.auctionId to it.viewCounter }
        return AuctionsViewsRespone(auctionsViewsPerId = auctionsViews)
    }

    fun getRecentViewsFromIpAddress(
        ipAddress: String,
        numberOfAuctions: Int? = null
    ): List<AuctionViewsFromIpResponse> {
        val results =
            auctionViewsPerIpRepository.findByIpAddressOrderByLastViewedDesc(ipAddress).map { it.toResponse() }
        return if (numberOfAuctions != null) results.take(numberOfAuctions) else results

    }

    fun getRecentViewsFromIpAddressForAuction(ipAddress: String, auctionId: String)
            : AuctionViewsFromIpResponse =
        auctionViewsPerIpRepository
            .findById(AuctionViewPerIpKey(ipAddress = ipAddress, auctionId = auctionId).formatToStringRepresentation())
            .map { it.toResponse() }
            .orElse(noViewsFromIpResponse(ipAddress, auctionId))

    fun getMostViewedAuctionsFromIpAddress(ipAddress: String, numberOfAuctions: Int? = null)
            : List<AuctionViewsFromIpResponse> {
        val results =
            auctionViewsPerIpRepository.findByIpAddressOrderByViewCounterDesc(ipAddress).map { it.toResponse() }
        return if (numberOfAuctions != null) results.take(numberOfAuctions) else results
    }

    private fun noViewsFromIpResponse(ipAddress: String, auctionId: String): AuctionViewsFromIpResponse =
        AuctionViewsFromIpResponse(auctionId = auctionId, ipAddress = ipAddress)

}

private fun AuctionViewsPerIp.toResponse(): AuctionViewsFromIpResponse {
    return AuctionViewsFromIpResponse(
        auctionId = this.auctionId,
        ipAddress = this.ipAddress,
        viewsTimestamps = this.viewedTimestamps,
        viewCounter = this.viewCounter,
    )
}
