package pl.kawaleria.auctsys.views.domain

import java.util.*
import java.util.concurrent.ConcurrentHashMap

class InMemoryAuctionViewsPerIpRepository : AuctionViewsPerIpRepository {
    val map: ConcurrentHashMap<String, AuctionViewsPerIp> = ConcurrentHashMap()
    override fun findByIpAddressOrderByLastViewedDesc(ipAddress: String): List<AuctionViewsPerIp> {
        return map.values.filter { it.ipAddress == ipAddress }.sortedByDescending { it.lastViewed }.toList()
    }

    override fun save(auctionViewsPerIp: AuctionViewsPerIp): AuctionViewsPerIp {
        map[auctionViewsPerIp.id] = auctionViewsPerIp
        return auctionViewsPerIp
    }

    override fun findById(id: String): Optional<AuctionViewsPerIp> {
        return Optional.ofNullable(map[id])
    }

    override fun findByIpAddressOrderByViewCounterDesc(ipAddress: String): List<AuctionViewsPerIp> {
        return map.values.filter { it.ipAddress == ipAddress }.sortedByDescending { it.viewCounter }.toList()
    }
}
