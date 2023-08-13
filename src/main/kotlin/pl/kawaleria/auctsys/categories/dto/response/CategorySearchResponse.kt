package pl.kawaleria.auctsys.categories.dto.response

data class CategorySearchResponse(
        val category: SimpleCategoryResponse,
        val categoryPath: CategoryPathResponse,
) {
}