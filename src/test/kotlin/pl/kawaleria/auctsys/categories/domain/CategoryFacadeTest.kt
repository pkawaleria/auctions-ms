package pl.kawaleria.auctsys.categories.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.junit.jupiter.Testcontainers
import pl.kawaleria.auctsys.categories.dto.response.CategoryPathResponse
import pl.kawaleria.auctsys.images.ImageControllerTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CategoryFacadeTest {

    private val mongo: MongoDBContainer = MongoDBContainer("mongo").apply {
        start()
    }

    init {
        System.setProperty("spring.data.mongodb.uri", mongo.replicaSetUrl)
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(ImageControllerTest::class.java)
    }

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Autowired
    private lateinit var categoryFacade: CategoryFacade


    @Test
    // TODO
    fun `should get category path`() {
        // given
        val businessLaptopsCategoryId: String = thereAreCategories()


        // when
        val path: CategoryPathResponse = categoryFacade.getFullCategoryPath(businessLaptopsCategoryId)

        // then
//        Assertions.assertThat(path.path).}
    }


    fun thereAreCategories(): String {
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
        mongoTemplate.insertAll(listOf(electronics, clothing))

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
        mongoTemplate.insertAll(listOf(smartphones, laptops, tShirts, jeans))

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
        mongoTemplate.insertAll(listOf(gamingLaptops, businessLaptops))

        return businessLaptops.id
    }
}