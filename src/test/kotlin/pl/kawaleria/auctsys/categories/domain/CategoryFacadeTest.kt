//package pl.kawaleria.auctsys.categories.domain
//
//import org.junit.jupiter.api.Test
//import org.slf4j.Logger
//import org.slf4j.LoggerFactory
//import pl.kawaleria.auctsys.categories.dto.responses.CategoryResponse
//import pl.kawaleria.auctsys.images.ImageControllerTest
//
//
//class CategoryFacadeTest {
//
//    companion object {
//        val logger: Logger = LoggerFactory.getLogger(ImageControllerTest::class.java)
//    }
//
//    private val categoryModule = CategoryConfiguration().categoryModuleWithInMemoryRepository()
//    private val categoryRepository: CategoryRepository = categoryModule.first
//    private val categoryFacade: CategoryFacade = categoryModule.second
//
//    @Test
//    fun `should save category with subcategories`() {
//        // given
//        val rootCategoryId = thereIsElectronicsCategoryTree()
//
//        // when
//        val get: CategoryResponse = categoryFacade.get(rootCategoryId)
//
//        // then
////        Assertions.assertThat(path.path).}
//    }
//
//    fun thereIsElectronicsCategoryTree(): String {
//
//        val electronicsCategory = CategoryBuilder(categoryRepository = categoryRepository)
//            .name("Elektronika")
//            .description("Elektroniczne przedmioty i gadżety")
//            .topLevel()
//
//            // Telefony i Akcesoria
//            .subCategory(
//                CategoryBuilder(categoryRepository = categoryRepository)
//                    .name("Telefony i Akcesoria")
//                    .description("Telefony komórkowe, smartfony i ich akcesoria")
//                    .subCategory(
//                        CategoryBuilder(categoryRepository = categoryRepository)
//                            .name("Smartfony")
//                            .description("Nowoczesne telefony z systemem operacyjnym")
//                            .finalNode()
//                    )
//                    .subCategory(
//                        CategoryBuilder(categoryRepository = categoryRepository)
//                            .name("Telefony komórkowe")
//                            .description("Tradycyjne telefony komórkowe")
//                            .finalNode()
//                    )
//                    .subCategory(
//                        CategoryBuilder(categoryRepository = categoryRepository)
//                            .name("Akcesoria telefonów")
//                            .description("Etui, ładowarki, słuchawki i inne akcesoria")
//                            .finalNode()
//                    )
//            )
//
//            // Komputery
//            .subCategory(
//                CategoryBuilder(categoryRepository = categoryRepository)
//                    .name("Komputery")
//                    .description("Stacjonarne i przenośne komputery oraz akcesoria")
//                    .subCategory(
//                        CategoryBuilder(categoryRepository = categoryRepository)
//                            .name("Laptopy")
//                            .description("Przenośne komputery")
//                            .finalNode()
//                    )
//                    .subCategory(
//                        CategoryBuilder(categoryRepository = categoryRepository)
//                            .name("Stacjonarne")
//                            .description("Komputery stacjonarne")
//                            .finalNode()
//                    )
//                    .subCategory(
//                        CategoryBuilder(categoryRepository = categoryRepository)
//                            .name("Akcesoria komputerowe")
//                            .description("Klawiatury, myszki, monitory i inne")
//                            .finalNode()
//                    )
//            )
//
//            // Audio i Video
//            .subCategory(
//                CategoryBuilder(categoryRepository = categoryRepository)
//                    .name("Audio i Video")
//                    .description("Sprzęt audio i video")
//                    .subCategory(
//                        CategoryBuilder(categoryRepository = categoryRepository)
//                            .name("Odtwarzacze MP3 i MP4")
//                            .description("Przenośne odtwarzacze muzyki i wideo")
//                            .finalNode()
//                    )
//                    .subCategory(
//                        CategoryBuilder(categoryRepository = categoryRepository)
//                            .name("Słuchawki")
//                            .description("Różne rodzaje słuchawek")
//                            .finalNode()
//                    )
//                    .subCategory(
//                        CategoryBuilder(categoryRepository = categoryRepository)
//                            .name("Głośniki i wieże")
//                            .description("Głośniki komputerowe, wieżemuzyczne i inne")
//                            .finalNode()
//                    )
//            )
//            .save()
//
//        return electronicsCategory!!.id
//    }
//}