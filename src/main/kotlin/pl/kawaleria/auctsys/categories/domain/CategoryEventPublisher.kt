package pl.kawaleria.auctsys.categories.domain

import pl.kawaleria.auctsys.categories.dto.events.CategoryDeletedEvent

interface CategoryEventPublisher {
    fun publish(categoryDeleted: CategoryDeletedEvent)
}