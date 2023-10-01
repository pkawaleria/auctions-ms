package pl.kawaleria.auctsys.images.domain

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "image.verification")
data class ImageVerificationRules(
    val enabled: Boolean
)