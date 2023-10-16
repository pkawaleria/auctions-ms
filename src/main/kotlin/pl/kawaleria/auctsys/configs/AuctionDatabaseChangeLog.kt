package pl.kawaleria.auctsys.configs

import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index

@ChangeLog(order = "001")
class AuctionDatabaseChangeLog {
    @ChangeSet(order = "001", id = "createCategoryIndicesOnAuction", author = "lukasz-karasek")
    fun createAllCategoryIndices(mongoTemplate: MongoTemplate) {
        // Index for categoryPath.pathElements.name
        val indexDefinition1 = Index("categoryPath.pathElements.name", Sort.Direction.ASC) // Using 1 for ASC
        mongoTemplate.indexOps("auctions").ensureIndex(indexDefinition1)

        // Index for category.name
        val indexDefinition2 = Index("category.name", Sort.Direction.ASC)
        mongoTemplate.indexOps("auctions").ensureIndex(indexDefinition2)

        // Index for category.id
        val indexDefinition3 = Index("category.id", Sort.Direction.ASC)
        mongoTemplate.indexOps("auctions").ensureIndex(indexDefinition3)
    }
}