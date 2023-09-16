package pl.kawaleria.auctsys.categories.domain

import org.springframework.context.annotation.Configuration

@Configuration
class CategoryConfiguration {

    fun categoryFacadeWithInMemoryRepository(): CategoryFacade {
        return CategoryFacade(
                categoryRepository = InMemoryCategoryRepository(),
                categoryEventPublisher = TestCategoryEventPublisher())
    }

}