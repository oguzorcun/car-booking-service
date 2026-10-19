package com.velocitymotors.carbooking.e2e;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Postgres and Kafka for tests, imported by the test classes that need them. @ServiceConnection
 * also keeps the tests off the docker-compose stack running on the same ports.
 */
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

    @Bean
    @ServiceConnection // overrides the datasource properties from application.yaml
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:18-alpine"); // same image as docker-compose.yaml
    }

    @Bean
    @ServiceConnection // overrides the kafka properties from application.yaml
    KafkaContainer kafka() {
        return new KafkaContainer(DockerImageName.parse("apache/kafka:4.3.1")); // same image as docker-compose.yaml
    }

    @Bean
    NewTopic bankTransferPaymentEvents(@Value("${bank-transfer-payment.topic}") String topic) {
        return TopicBuilder.name(topic).partitions(1).replicas(1).build(); // the listener subscribes at startup, so the topic has to exist before the first event
    }
}
