package pl.kawaleria.auctsys.categories.dto.events

import java.time.Instant

data class CategoryDeletedEvent(
        val categoryId: String,
        val categoryName: String,
        val isFinalNode: Boolean,
        val timestamp: Instant
)
