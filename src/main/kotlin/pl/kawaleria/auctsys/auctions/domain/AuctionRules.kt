package pl.kawaleria.auctsys.auctions.domain

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "auction.expiration.duration")
data class AuctionRules (val days: Int)

@ConfigurationProperties(prefix = "auction.search.localization.radius")
data class RadiusRules (val min: Double, val max: Double)