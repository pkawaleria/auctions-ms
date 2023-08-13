package pl.kawaleria.auctsys.categories.domain

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import pl.kawaleria.auctsys.auctions.dto.exceptions.CategoryNotFound
import pl.kawaleria.auctsys.auctions.dto.exceptions.InvalidCategoryActionException
import pl.kawaleria.auctsys.categories.dto.request.CategoryCreateRequest
import pl.kawaleria.auctsys.categories.dto.response.*

@Service
class CategoryFacade(private val categoryRepository: CategoryRepository) {

    @Transactional
    fun delete(categoryId: String) {
        val category = categoryRepository.findById(categoryId).orElseThrow { CategoryNotFound() }

        if (category.isTopLevel) {
            throw InvalidCategoryActionException("Cannot delete top level category")
        }
        if (category.isFinalNode) {
            transferFinalNodeResponsibility(category)
        }

        val subcategories = categoryRepository.findSubcategories(categoryId)
        val newParentId = category.parentCategoryId
        subcategories.forEach { it.changeParent(newParentId) }
        categoryRepository.saveAll(subcategories)
        categoryRepository.delete(category)

    }

    private fun transferFinalNodeResponsibility(category: Category) {
        val parentCategory: Category = category.parentCategoryId?.let { categoryRepository.findById(it) }!!.orElseThrow()
        parentCategory.isFinalNode = true
        categoryRepository.save(parentCategory)
    }

    fun getTopLevelCategories(): List<CategoryResponse> {
        return categoryRepository.getTopLevelOnes()
                .map { it.toResponse(categoryRepository.findSubcategories(it.id).toSubcategoriesResponse()) }
    }

    fun get(categoryId: String): CategoryResponse {
        val category = categoryRepository.findById(categoryId).orElseThrow { CategoryNotFound() }
        val subcategories = categoryRepository.findSubcategories(categoryId)
        return category.toResponse(subcategories.toSubcategoriesResponse())
    }

    fun create(request: CategoryCreateRequest): CategoryResponse {
        if (!request.isTopLevel && categoryRepository.existsById(request.parentCategoryId)) {
            throw InvalidCategoryActionException("Cannot find parent category of id ${request.parentCategoryId}")
        }
        val category: Category = request.toCategory()
        return categoryRepository.insert(category).toResponse(emptyList())
    }

    fun getFullCategoryPath(categoryId: String): CategoryPathResponse {
        val category = categoryRepository.findById(categoryId).orElseThrow { CategoryNotFound() }
        val categoryPath = mutableListOf(category.toCategoryNameResponse())

        var parentCategoryId: String? = category.parentCategoryId
        while (parentCategoryId != null) {
            val parentCategory = categoryRepository.findById(parentCategoryId).orElseThrow { CategoryNotFound() }
            parentCategoryId = parentCategory.parentCategoryId
            categoryPath.add(parentCategory.toCategoryNameResponse())
        }

        return CategoryPathResponse(categoryPath.reversed())
    }

    fun getFinalCategories(): List<CategoryResponse> {
        return categoryRepository.getFinalCategories().map { it.toResponse(emptyList()) }
    }

    fun search(phraseInName: String) : List<CategorySearchResponse> {
        return categoryRepository.findAllByNameContainingIgnoreCase(phraseInName)
                .map { CategorySearchResponse(category = it.toSimpleCategoryResponse(), categoryPath = getFullCategoryPath(it.id)) }
    }

}

private fun Category.toSimpleCategoryResponse(): SimpleCategoryResponse {
    return SimpleCategoryResponse(
            id = this.id,
            name = this.name,
            isTopLevel = this.isTopLevel,
            isFinalNode = this.isFinalNode,
            description = this.description,
    )
}

private fun CategoryCreateRequest.toCategory(): Category {
    return if (this.isTopLevel) this.toTopLevelCategory() else this.toSubcategory()
}

private fun CategoryCreateRequest.toTopLevelCategory(): Category {
    return Category(
            isTopLevel = true,
            isFinalNode = false,
            name = this.name,
            description = this.description,
            parentCategoryId = null
    )
}

private fun CategoryCreateRequest.toSubcategory(): Category {
    return Category(
            isTopLevel = false,
            isFinalNode = this.isFinalNode,
            name = this.name,
            description = this.description,
            parentCategoryId = this.parentCategoryId
    )
}

private fun List<Category>.toSubcategoriesResponse(): List<SubcategoryResponse> {
    return this.map { SubcategoryResponse(id = it.id, name = it.name) }
}

private fun Category.toResponse(subcategories: List<SubcategoryResponse>): CategoryResponse {
    return CategoryResponse(
            id = this.id,
            name = this.name,
            isTopLevel = this.isTopLevel,
            isFinalNode = this.isFinalNode,
            description = this.description,
            subcategories = subcategories
    )
}

private fun Category.toCategoryNameResponse(): CategoryNameResponse {
    return CategoryNameResponse(
            id = this.id,
            name = this.name
    )
}

