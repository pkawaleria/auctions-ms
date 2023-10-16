package pl.kawaleria.auctsys.categories.domain

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "categories")
data class Category(
        @Id
        var id: String = ObjectId().toString(),
        var name: String,
        var description: String,
        var isTopLevel: Boolean,
        var isFinalNode: Boolean,
        @Indexed
        var parentCategoryId: String? = null,
) {

    fun changeParent(categoryId: String?) {
        this.parentCategoryId = categoryId
    }
}