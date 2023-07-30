package pl.kawaleria.auctsys.configs

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "image.thumbnail")
data class ThumbnailRules(
    var height: Int,
    var width: Int
)