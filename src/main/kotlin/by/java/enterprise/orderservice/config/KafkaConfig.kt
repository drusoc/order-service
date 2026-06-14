package by.java.enterprise.orderservice.config

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
class KafkaConfig {

    @Value("\${spring.kafka.bootstrap-servers}")
    private lateinit var bootstrapServers: String

    @Value("\${spring.kafka.producer.key-serializer}")
    private lateinit var keySerializer: String

    @Value("\${spring.kafka.producer.value-serializer}")
    private lateinit var valueSerializer: String

    @Value("\${spring.kafka.producer.acks}")
    private lateinit var acks: String

    @Value("\${spring.kafka.producer.properties.delivery.timeout.ms}")
    private lateinit var deliveryTimeout: String

    @Value("\${spring.kafka.producer.properties.request.timeout.ms}")
    private lateinit var requestTimeout: String

    @Value("\${spring.kafka.producer.properties.linger.ms}")
    private lateinit var linger: String

    @Value("\${spring.kafka.producer.retries}")
    private lateinit var retries: String

    @Value("\${spring.kafka.producer.properties.retry.backoff.ms}")
    private lateinit var retryBackoff: String

    @Value("\${spring.kafka.producer.properties.enable.idempotence}")
    private lateinit var idempotence: String

    @Value("\${spring.kafka.producer.properties.max.in.flight.requests.per.connection}")
    private lateinit var maxInFlightRequests: String

    private fun producerConfig(): Map<String, Any> = mapOf(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to keySerializer,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to valueSerializer,
        ProducerConfig.ACKS_CONFIG to acks,
        ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG to deliveryTimeout,
        ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG to requestTimeout,
        ProducerConfig.LINGER_MS_CONFIG to linger,
        ProducerConfig.RETRIES_CONFIG to retries,
        ProducerConfig.RETRY_BACKOFF_MS_CONFIG to retryBackoff,
        ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to idempotence,
        ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION to maxInFlightRequests
    )

    @Bean
    fun producerFactory(): ProducerFactory<String, String> =
        DefaultKafkaProducerFactory(producerConfig())

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, String> =
        KafkaTemplate(producerFactory())

    @Bean
    fun orderEventsTopic(): NewTopic =
        TopicBuilder.name("order.events")
            .partitions(3)
            .replicas(1)
            .build()
}