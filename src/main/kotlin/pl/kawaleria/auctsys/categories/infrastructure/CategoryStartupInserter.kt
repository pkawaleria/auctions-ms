package pl.kawaleria.auctsys.categories.infrastructure

import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import pl.kawaleria.auctsys.auctions.domain.AuctionFacade
import pl.kawaleria.auctsys.auctions.dto.requests.CreateAuctionRequest
import pl.kawaleria.auctsys.categories.domain.Category
import pl.kawaleria.auctsys.categories.domain.CategoryRepository

// TODO: This class serves only for development (inserts predefined test data). It will be better to extract that into
//  files (dev-inserts.json or whatever) and load this files at startup
@Component
class CategoryStartupInserter(private val categoryRepository: CategoryRepository,
                              private val auctionFacade: AuctionFacade) {

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
        categoryRepository.saveAll(mutableListOf(electronics, clothing))

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
        categoryRepository.saveAll(mutableListOf(smartphones, laptops, tShirts, jeans))

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
        categoryRepository.saveAll(mutableListOf(gamingLaptops, businessLaptops))

        auctionFacade.addNewAuction(
                createRequest = CreateAuctionRequest(
                        name = "Modern Dell laptop",
                        description = "Modern dell laptop with radeon graphics",
                        price = 12.4,
                        categoryId = gamingLaptops.id
                ), auctioneerId = "auctioneer-id")
    }
}