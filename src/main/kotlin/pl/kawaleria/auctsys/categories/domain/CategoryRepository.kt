package pl.kawaleria.auctsys.categories.domain

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query


interface CategoryRepository : MongoRepository<Category, String> {
    @Query("{'parentCategoryId': ?0}")
    fun findSubcategories(categoryId: String): List<Category>

    @Query("{ 'isTopLevel' : true }")
    fun getTopLevelOnes(): List<Category>

    @Query("{ 'isFinalNode' : true }")
    fun getFinalCategories(): List<Category>

    fun findAllByNameContainingIgnoreCase(phrase: String) : List<Category>

}