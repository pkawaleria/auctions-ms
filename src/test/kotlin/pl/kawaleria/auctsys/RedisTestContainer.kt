package pl.kawaleria.auctsys

import com.redis.testcontainers.RedisContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName

object RedisTestContainer {
    val instance by lazy { startRedisContainer() }

    private fun startRedisContainer(): RedisContainer = RedisContainer(DockerImageName.parse("redis")).apply {
        withExposedPorts(6379)
        setWaitStrategy(Wait.forListeningPort())
        start()
    }
}
