package pl.kawaleria.auctsys.categories.domain

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface MongoCategoryRepository : CategoryRepository, MongoRepository<Category, String> {
    @Query("{'parentCategoryId': ?0}")
    override fun findSubcategories(categoryId: String): List<Category>

    @Query("{ 'isTopLevel' : true }")
    override fun getTopLevelOnes(): List<Category>

    @Query("{ 'isFinalNode' : true }")
    override fun getFinalCategories(): List<Category>

    override fun findAllByNameContainingIgnoreCase(phrase: String) : List<Category>
}