package pl.kawaleria.auctsys.categories.dto.responses

data class CategorySearchResponse(
        val category: SimpleCategoryResponse,
        val categoryPath: CategoryPathResponse,
)