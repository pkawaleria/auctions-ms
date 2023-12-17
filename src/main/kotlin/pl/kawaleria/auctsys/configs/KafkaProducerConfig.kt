package pl.kawaleria.auctsys.configs


import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer


private const val TOPIC_PARTITIONS = 1
private const val TOPIC_REPLICATION_FACTOR = 1.toShort()

@Configuration
@EnableKafka
class KafkaProducerConfig(@Value("\${kafka.topics.text-content.verification.request}") val kafkaInappropriateContentTopic: String,
                          @Value("\${kafka.topics.image.verification.request}") val kafkaInappropriateImageTopic: String,
                          @Value("\${spring.kafka.bootstrap-servers}") val bootstrapServers: String) {

    @Bean
    fun producerFactory(): ProducerFactory<String, Any> {
        val configProps: MutableMap<String, Any> = HashMap()
        configProps[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        configProps[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        configProps[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        return DefaultKafkaProducerFactory(configProps)
    }

    @Bean
    fun kafkaTemplate(producerFactory: ProducerFactory<String, Any>): KafkaTemplate<String, Any> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    fun kafkaAdmin(): KafkaAdmin {
        val configs: MutableMap<String, Any> = HashMap()
        configs[AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        return KafkaAdmin(configs)
    }

    @Bean
    fun inappropriateTextContentTopic(): NewTopic {
        return NewTopic(kafkaInappropriateContentTopic, TOPIC_PARTITIONS, TOPIC_REPLICATION_FACTOR)
    }
    @Bean
    fun inappropriateImageTopic(): NewTopic {
        return NewTopic(kafkaInappropriateImageTopic, TOPIC_PARTITIONS, TOPIC_REPLICATION_FACTOR)
    }
}
