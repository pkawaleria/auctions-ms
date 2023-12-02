package pl.kawaleria.auctsys.categories.domain

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import pl.kawaleria.auctsys.categories.dto.events.CategoryDeletedEvent
import pl.kawaleria.auctsys.categories.dto.exceptions.CategoryNotFound
import pl.kawaleria.auctsys.categories.dto.exceptions.InvalidCategoryActionException
import pl.kawaleria.auctsys.categories.dto.requests.CategoryCreateRequest
import pl.kawaleria.auctsys.categories.dto.responses.*
import pl.kawaleria.auctsys.commons.ServiceErrorResponseCode
import java.time.Instant

@Component
class CategoryFacade(
    private val categoryRepository: CategoryRepository,
    private val categoryEventPublisher: CategoryEventPublisher
) {

    @Transactional
    fun delete(categoryId: String) {
        val category: Category = categoryRepository.findById(categoryId).orElseThrow { CategoryNotFound() }

        if (category.isTopLevel) {
            throw InvalidCategoryActionException(ServiceErrorResponseCode.CAT03)
        }
        if (category.isFinalNode) {
            transferFinalNodeResponsibility(category)
        }

        val subcategories: List<Category> = categoryRepository.findSubcategories(categoryId)
        val newParentId: String? = category.parentCategoryId
        subcategories.forEach { it.changeParent(newParentId) }
        categoryRepository.saveAll(subcategories.toMutableList())
        categoryRepository.delete(category)
        categoryEventPublisher.publish(category.toDeletedEvent())
    }

    private fun transferFinalNodeResponsibility(category: Category) {
        val parentCategory: Category =
            category.parentCategoryId?.let { categoryRepository.findById(it) }!!.orElseThrow()
        parentCategory.isFinalNode = true
        categoryRepository.save(parentCategory)
    }

    fun getTopLevelCategories(): List<CategoryResponse> {
        return categoryRepository.getTopLevelOnes()
            .map { it.toResponse(categoryRepository.findSubcategories(it.id).toSubcategoriesResponse()) }
    }

    fun get(categoryId: String): CategoryWithPathResponse {
        val category: Category = categoryRepository.findById(categoryId).orElseThrow { CategoryNotFound() }
        val subcategories: List<Category> = categoryRepository.findSubcategories(categoryId)
        val path: CategoryPathResponse = getFullCategoryPath(categoryId)
        return category.toResponseWithPath(subcategories.toSubcategoriesResponse(), path.toSimplePath())
    }

    fun getSubcategories(categoryId: String): List<CategoryWithPathResponse> {
        return categoryRepository.findSubcategories(categoryId)
            .map {
                it.toResponseWithPath(
                    subcategories = categoryRepository.findSubcategories(it.id).toSubcategoriesResponse(),
                    path = getFullCategoryPath(it.id).toSimplePath()
                )
            }
    }

    fun create(request: CategoryCreateRequest): CategoryResponse {
        if (!request.isTopLevel && request.parentCategoryId?.let { categoryRepository.existsById(it) } == false) {
            throw InvalidCategoryActionException(ServiceErrorResponseCode.CAT02)
        }
        val category: Category = request.toCategory()
        return categoryRepository.insert(category).toResponse(emptyList())
    }

    fun getFullCategoryPath(categoryId: String): CategoryPathResponse {
        val category: Category = categoryRepository.findById(categoryId).orElseThrow { CategoryNotFound() }
        val categoryPath: MutableList<CategoryNameResponse> = mutableListOf(category.toCategoryNameResponse())

        var parentCategoryId: String? = category.parentCategoryId
        while (parentCategoryId != null) {
            val parentCategory: Category =
                categoryRepository.findById(parentCategoryId).orElseThrow { CategoryNotFound() }
            parentCategoryId = parentCategory.parentCategoryId
            categoryPath.add(parentCategory.toCategoryNameResponse())
        }

        return CategoryPathResponse(
            requestedCategory = category.toSimpleCategoryResponse(),
            path = categoryPath.reversed()
        )
    }

    fun getFinalCategories(): List<CategoryResponse> {
        return categoryRepository.getFinalCategories().map { it.toResponse(emptyList()) }
    }

    fun search(phraseInName: String): List<CategorySearchResponse> {
        return categoryRepository.findAllByNameContainingIgnoreCase(phraseInName)
            .map {
                CategorySearchResponse(
                    category = it.toSimpleCategoryResponse(),
                    categoryPath = getFullCategoryPath(it.id)
                )
            }
    }

}

private fun Category.toSimpleCategoryResponse(): SimpleCategoryResponse = SimpleCategoryResponse(
    id = this.id,
    name = this.name,
    isTopLevel = this.isTopLevel,
    isFinalNode = this.isFinalNode,
    description = this.description,
)

private fun CategoryCreateRequest.toCategory(): Category =
    if (this.isTopLevel) this.toTopLevelCategory() else this.toSubcategory()

private fun CategoryCreateRequest.toTopLevelCategory(): Category = Category(
    isTopLevel = true,
    isFinalNode = false,
    name = this.name,
    description = this.description,
    parentCategoryId = null
)

private fun CategoryCreateRequest.toSubcategory(): Category = Category(
    isTopLevel = false,
    isFinalNode = this.isFinalNode,
    name = this.name,
    description = this.description,
    parentCategoryId = this.parentCategoryId
)

private fun List<Category>.toSubcategoriesResponse(): List<SubcategoryResponse> =
    this.map { SubcategoryResponse(id = it.id, name = it.name) }

private fun Category.toResponse(subcategories: List<SubcategoryResponse>): CategoryResponse = CategoryResponse(
    id = this.id,
    name = this.name,
    isTopLevel = this.isTopLevel,
    isFinalNode = this.isFinalNode,
    description = this.description,
    subcategories = subcategories
)

private fun Category.toResponseWithPath(
    subcategories: List<SubcategoryResponse>,
    path: SimpleCategoryPathResponse
): CategoryWithPathResponse =
    CategoryWithPathResponse(
        id = this.id,
        name = this.name,
        isTopLevel = this.isTopLevel,
        isFinalNode = this.isFinalNode,
        description = this.description,
        subcategories = subcategories,
        path = path
    )

private fun Category.toCategoryNameResponse(): CategoryNameResponse = CategoryNameResponse(
    id = this.id,
    name = this.name
)

private fun CategoryPathResponse.toSimplePath(): SimpleCategoryPathResponse =
    SimpleCategoryPathResponse(path = this.path)

private fun Category.toDeletedEvent(): CategoryDeletedEvent = CategoryDeletedEvent(
    categoryId = this.id,
    categoryName = this.name,
    isFinalNode = this.isFinalNode,
    timestamp = Instant.now()
)

