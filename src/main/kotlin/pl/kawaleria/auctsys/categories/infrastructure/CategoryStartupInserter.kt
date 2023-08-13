package pl.kawaleria.auctsys.categories.infrastructure

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import pl.kawaleria.auctsys.categories.domain.Category
import pl.kawaleria.auctsys.categories.domain.CategoryRepository

// TODO: This class serves only for development (inserts predefined test data). It will be better to extract that into
//  files (dev-inserts.json or whatever) and load this files at startup
@Component
class CategoryStartupInserter(private val categoryRepository: CategoryRepository) {

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReadyEvent() {
        insertTestCategories()
    }

    fun insertTestCategories() {
        categoryRepository.deleteAll()

        val electronics = Category(
                name = "Electronics",
                description = "This is nice category",
                isTopLevel = true,
                isFinalNode = false
        )

        val clothing = Category(
                name = "Clothing",
                description = "This is nice category",
                isTopLevel = true,
                isFinalNode = false
        )
        categoryRepository.saveAll(listOf(electronics, clothing))

        val smartphones = Category(
                name = "Smartphones",
                description = "This is nice category",
                isTopLevel = false,
                isFinalNode = false,
                parentCategoryId = electronics.id
        )
        val laptops = Category(
                name = "Laptops",
                description = "This is nice category",
                isTopLevel = false,
                isFinalNode = false,
                parentCategoryId = electronics.id
        )

        val tShirts = Category(
                name = "T-Shirts",
                description = "This is nice category",
                isTopLevel = false,
                isFinalNode = false,
                parentCategoryId = clothing.id
        )
        val jeans = Category(
                name = "Jeans",
                description = "This is nice category",
                isTopLevel = false,
                isFinalNode = false,
                parentCategoryId = clothing.id
        )
        categoryRepository.saveAll(listOf(smartphones, laptops, tShirts, jeans))

        val gamingLaptops = Category(
                name = "gaming laptops",
                description = "This is nice category",
                isTopLevel = false,
                isFinalNode = true,
                parentCategoryId = laptops.id
        )
        val businessLaptops = Category(
                name = "business laptops",
                description = "This is nice category",
                isTopLevel = false,
                isFinalNode = true,
                parentCategoryId = laptops.id
        )
        categoryRepository.saveAll(listOf(gamingLaptops, businessLaptops))
    }
}