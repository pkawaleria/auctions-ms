package pl.kawaleria.auctsys.categories.dto.responses

data class CategoryPathResponse(
        val requestedCategory: SimpleCategoryResponse,
        val path: List<CategoryNameResponse>
)

data class CategoryNameResponse(
        val id: String,
        val name: String
)
