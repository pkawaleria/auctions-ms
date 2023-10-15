package pl.kawaleria.auctsys.views.domain

import java.util.*


interface AuctionViewsPerIpRepository {
    fun findByIpAddressOrderByLastViewedDesc(ipAddress: String): List<AuctionViewsPerIp>
    fun save(auctionViewsPerIp: AuctionViewsPerIp): AuctionViewsPerIp
    fun findById(id: String): Optional<AuctionViewsPerIp>
    fun findByIpAddressOrderByViewCounterDesc(ipAddress: String): List<AuctionViewsPerIp>
}
