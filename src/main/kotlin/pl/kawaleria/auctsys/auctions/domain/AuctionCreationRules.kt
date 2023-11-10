package pl.kawaleria.auctsys.auctions.domain

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "auction.expiration.duration")
data class AuctionCreationRules (val days: Int)

@ConfigurationProperties(prefix = "auction.search.localization.radius")
data class AuctionSearchingRules (val min: Double, val max: Double)

@ConfigurationProperties(prefix = "auction.text.verification")
data class AuctionVerificationRules (val enabled: Boolean)