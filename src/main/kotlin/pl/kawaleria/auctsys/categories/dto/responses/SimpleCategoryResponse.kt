package pl.kawaleria.auctsys.categories.dto.responses

data class SimpleCategoryResponse(
        val id: String,
        val name: String,
        val isTopLevel: Boolean,
        val description: String,
        val isFinalNode: Boolean
)