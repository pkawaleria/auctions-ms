package pl.kawaleria.auctsys.categories.dto.response

data class CategoryPathResponse(
        val path: List<CategoryNameResponse>
)

data class CategoryNameResponse(
        val id: String,
        val name: String
)
