package pl.kawaleria.auctsys.images.domain

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "image.thumbnail")
data class ThumbnailRules(
        val height: Int,
        val width: Int
)