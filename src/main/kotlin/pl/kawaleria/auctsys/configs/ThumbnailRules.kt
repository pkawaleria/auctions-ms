package pl.kawaleria.auctsys.configs

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "image.thumbnail")

// TODO: move that to images module, that config is it's internal config, not global
data class ThumbnailRules(
        var height: Int,
        var width: Int
)