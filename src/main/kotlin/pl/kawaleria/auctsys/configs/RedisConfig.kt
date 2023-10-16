package pl.kawaleria.auctsys.configs

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
@EnableRedisRepositories
class RedisConfig(
    @Value("\${spring.data.redis.host}") val redisHost: String,
    @Value("\${spring.data.redis.port}") val redisPort: Int
) {

    @Bean
    fun connectionFactory(): RedisConnectionFactory = LettuceConnectionFactory(redisHost, redisPort)

    @Bean
    fun redisTemplate(
        redisConnectionFactory: RedisConnectionFactory,
        objectMapper: ObjectMapper
    ): RedisTemplate<String, String> {
        val template = RedisTemplate<String, String>()
        template.connectionFactory = redisConnectionFactory

        // Configure serialization
        val objectMapper: ObjectMapper = objectMapper.findAndRegisterModules()
        val serializer = GenericJackson2JsonRedisSerializer(objectMapper)

        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = serializer

        template.setDefaultSerializer(serializer)
        template.setEnableTransactionSupport(false)
        return template
    }
}
