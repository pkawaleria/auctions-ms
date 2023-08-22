package pl.kawaleria.auctsys.categories.infrastructure

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import pl.kawaleria.auctsys.categories.domain.CategoryEventPublisher
import pl.kawaleria.auctsys.categories.dto.events.CategoryDeletedEvent

@Component
class SpringCategoryEventEventPublisher(private val applicationEventPublisher: ApplicationEventPublisher) : CategoryEventPublisher{

    override fun publish(categoryDeleted: CategoryDeletedEvent) {
        applicationEventPublisher.publishEvent(categoryDeleted)
    }
}