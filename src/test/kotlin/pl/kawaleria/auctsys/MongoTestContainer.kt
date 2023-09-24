package pl.kawaleria.auctsys

import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.wait.strategy.Wait

object MongoTestContainer {
    val instance by lazy { startMongoContainer() }
    private fun startMongoContainer() = MongoDBContainer("mongo").apply {
        setWaitStrategy(Wait.forListeningPort())
        start()
    }
}