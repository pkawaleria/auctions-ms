package pl.kawaleria.auctsys.categories.domain

import org.springframework.context.annotation.Configuration
import pl.kawaleria.auctsys.categories.dto.events.CategoryDeletedEvent

@Configuration
class CategoryConfiguration {

    fun categoryFacadeWithInMemoryRepository(): CategoryFacade {
        return CategoryFacade(
                categoryRepository = InMemoryCategoryRepository(),
                categoryEventPublisher = TestCategoryEventPublisher())
    }

    fun categoryModuleWithInMemoryRepository(): Pair<InMemoryCategoryRepository, CategoryFacade> {
        val categoryRepository = InMemoryCategoryRepository()
        return Pair(
            categoryRepository,
            CategoryFacade(
                categoryRepository = categoryRepository,
                categoryEventPublisher = TestCategoryEventPublisher()
            )
        )
    }

    internal class TestCategoryEventPublisher : CategoryEventPublisher {
        override fun publish(categoryDeleted: CategoryDeletedEvent) {
        }

    }

}