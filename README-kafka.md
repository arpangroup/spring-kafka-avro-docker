## Kafka

[Implementing a basic Kafka Producer and Consumer using Spring Boot, Spring Kafka and Avro schema](https://medium.com/ing-tech-romania/implementing-a-basic-kafka-producer-and-consumer-using-spring-boot-spring-kafka-and-avro-schema-2b6d06e6c4cf)
[YouTube](https://www.youtube.com/watch?v=T3vfHQIpfdw)

[1. Github](https://github.com/dianafagateanu/kafka-demo)
[2. Github](https://github.com/sptrivedigit1989/shaan)


## spring-kafka with the version managed by the spring-boot-starter:
````xml
<dependency>
   <groupId>org.springframework.kafka</groupId>
   <artifactId>spring-kafka</artifactId>
 </dependency>
````
Avro schema related dependencies for serialization/deserialization, together with the avro-maven-plugin that:
````xml
<dependency>
   <groupId>org.apache.avro</groupId>
   <artifactId>avro</artifactId>
   <version>1.11.3</version>
</dependency>
<dependency>
   <groupId>io.confluent</groupId>
   <artifactId>kafka-avro-serializer</artifactId>
   <version>7.5.1</version>
</dependency>
 <plugin>
    <groupId>org.apache.avro</groupId>
    <artifactId>avro-maven-plugin</artifactId>
    <version>1.11.0</version>
    <executions>
       <execution>
          <id>avro</id>
          <phase>generate-sources</phase>
          <goals>
             <goal>schema</goal>
          </goals>
          <configuration>
             <sourceDirectory>${project.basedir}/src/main/resources/avro/</sourceDirectory>
             <outputDirectory>${project.basedir}/src/main/java</outputDirectory>
             <stringType>String</stringType>
          </configuration>
        </execution>
     </executions>
 </plugin>
````
And spring-boot-maven-plugin:
````xml
<plugin>
   <groupId>org.springframework.boot</groupId>
   <artifactId>spring-boot-maven-plugin</artifactId>
   <configuration>
      <layout>ZIP</layout>
      <executable>true</executable>
   </configuration>
</plugin>
````

## Avro schema
I use a dummy avro schema called **TransactionEvent** of type `“record”` in `.avsc` format which needs to be placed under `src/main/resources/avro`. Based on the structure of this file, the avro-maven-plugin will generate the associated Java classes under the package com.kafka.demo.avro.model

## Configuring Kafka Properties
````properties
kafka.transaction-topic=transaction_events_topic

spring.kafka.ssl.key-store-location=classpath:server-keystore.jks
spring.kafka.ssl.key-store-password=password
spring.kafka.ssl.key-password=password
spring.kafka.ssl.trust-store-location=classpath:server-truststore.jks
spring.kafka.ssl.trust-store-password=password
spring.kafka.properties.security.protocol=SSL
spring.kafka.properties.auto.register.schemas=true
spring.kafka.bootstrap-servers=https://kafka:29092
````
The properties point to the Kafka container started inside docker and I use schema auto registering of the avro schema defined for the TransactionEvent.

## Spring Kafka consumer properties:
````properties
spring.kafka.consumer.properties.specific.avro.reader=true
spring.kafka.consumer.group-id=kafka-demo-123
spring.kafka.consumer.key-deserializer=org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
spring.kafka.consumer.properties.spring.deserializer.key.delegate.class=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.properties.spring.deserializer.value.delegate.class=io.confluent.kafka.serializers.KafkaAvroDeserializer
spring.kafka.consumer.properties.schema.registry.url=https://schema-registry:8091
spring.kafka.consumer.properties.schema.registry.ssl.keystore.location=/var/kafka-demo-app/conf/server-keystore.jks
spring.kafka.consumer.properties.schema.registry.ssl.keystore.password=password
spring.kafka.consumer.properties.schema.registry.ssl.key.password=password
spring.kafka.consumer.properties.schema.registry.ssl.keystore.type=JKS
spring.kafka.consumer.properties.schema.registry.ssl.truststore.location=/var/kafka-demo-app/conf/server-truststore.jks
spring.kafka.consumer.properties.schema.registry.ssl.truststore.password=password
spring.kafka.consumer.auto-offset-reset=earliest
````

I use an ErrorHandlingDeserializer for handling deserialization exceptions gracefully and skipping problematic messages that cannot be deserialized. This helps ensure the robustness and stability of our Kafka consumer app.

The properties point towards the schema-registry container started inside docker that will store the avro schema declared above to be auto registere


## Spring Kafka producer properties:
````properties
spring.kafka.producer.acks=all
spring.kafka.producer.retries=50
spring.kafka.producer.properties.retry.backoff.ms=250
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=io.confluent.kafka.serializers.KafkaAvroSerializer
spring.kafka.producer.properties.schema.registry.url=https://schema-registry:8091
spring.kafka.producer.properties.schema.registry.ssl.keystore.location=/var/kafka-demo-app/conf/server-keystore.jks
spring.kafka.producer.properties.schema.registry.ssl.keystore.password=password
spring.kafka.producer.properties.schema.registry.ssl.key.password=password
spring.kafka.producer.properties.schema.registry.ssl.keystore.type=JKS
spring.kafka.producer.properties.schema.registry.ssl.truststore.location=/var/kafka-demo-app/conf/server-truststore.jks
spring.kafka.producer.properties.schema.registry.ssl.truststore.password=password
````

For serialization/deserialization, I use Kafka StringSerializer/StringDeserializer, but in a production environment, depending on how sensitive is the data we want to publish, we should think about using **encryption** for the entire message or only for certain fields within the message. This ensures that even if the messages are intercepted, the data in transit remains protected. Consider factors like performance, compliance and risk requirements, data sensitivity and the potential impact of unauthorized access when making the decision.

## Creating a Kafka Producer
To create a Kafka producer, I will first need a KafkaProducerConfig class where I will use a KafkaTemplate to wrap a producer factory instance configured with the KafkaProperties that were bound from application.properties.

KafkaTemplate instances are thread safe and it is recommended to use only one instance, without worrying about synchronization issues.

````java
@Configuration
@EnableKafka
@RequiredArgsConstructor
public class KafkaProducerConfig {

    @Bean
    public KafkaTemplate<String, TransactionEvent> kafkaTemplate(KafkaProperties kafkaProperties) {
        Map<String, Object> kafkaPropertiesMap = kafkaProperties.buildProducerProperties(null);
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(kafkaPropertiesMap));
    }
}
````

Next, I will create a KafkaProducer service class that will be used to send messages using the KafkaTemplate bean configured above:

````java
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;
    private final KafkaCustomProperties kafkaCustomProperties;

    public void sendMessage(TransactionEvent transactionEvent) {
        ProducerRecord<String, TransactionEvent> producerRecord = new ProducerRecord<>(kafkaCustomProperties.getTransactionTopic(), transactionEvent);
        CompletableFuture<SendResult<String, TransactionEvent>> completableFuture = kafkaTemplate.send(producerRecord);
        log.info("Sending kafka message on topic {}", kafkaCustomProperties.getTransactionTopic());

        completableFuture.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Kafka message successfully sent on topic {} and value {}", kafkaCustomProperties.getTransactionTopic(), result.getProducerRecord().value().toString());
            } else {
                log.error("An error occurred while sending kafka message for event with value {}", producerRecord);
            }
        });
    }
}
````
The send method returns a CompletableFuture<SendResult>. We can register a callback with the listener to receive the result of the send asynchronously and add metrics for success/failed messages, together with an error handling in case an exception is present.

## Creating a Kafka Consumer
To create a Kafka consumer I will first need a KafkaConsumerConfig class where `ConcurrentKafkaListenerContainerFactory` is used to create containers for annotated KafkaListener methods. Once the container is created, we can further add or modify its properties.

````java
@Configuration
@EnableKafka
@AllArgsConstructor
public class KafkaConsumerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(KafkaProperties kafkaProperties) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(defaultConsumerFactory(kafkaProperties));
        return factory;
    }

    private ConsumerFactory<String, String> defaultConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> consumerProps = kafkaProperties.buildConsumerProperties(null);
        return new DefaultKafkaConsumerFactory<>(consumerProps);
    }
}
````

Next, I will need a KafkaConsumer class where we could configure multiple listeners for every Kafka **topic** that we need to consume messages from. In this example, I use only one.

Also, we can configure custom properties per topic (for instance, we might need to consume encrypted messages from different topics and have different decryption keys per topic). In order to do that, we will need to create multiple containers and map each listener to its container using **containerFactory** property and the bean name of the container.

````java
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    @KafkaListener(topics = "${kafka.transaction-topic}", containerFactory = "kafkaListenerContainerFactory", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(Message<TransactionEvent> transactionEventMessage) {
        log.info("Starting consuming from transaction_events_topic - {}", transactionEventMessage.toString());
    }
}
````

## Setting up Docker
The application runs in a container using Docker. The docker-compose.yml file will setup all containers:

- **kafka** — for producing and consuming Kafka messages. The API will connect to Kafka by using the container name
- **zookeeper** — its role is to coordinate and manage Kafka brokers
- **schema-registry** — to manage and store schemas for the messages exchanged between producers and consumers in a Kafka ecosystem. The API will connect to schema-registry by using the container name
- **kafka-demo-api** — representing the container of our application

I simulate a production environment and use SSL for the connection between the API, Kafka and schema-registry and for this, I generated self signed certificates.

## Testing the application
With the producer and consumer components in place, I can now test the application. I will start by sending a message on the configured topic and verify that the consumer receives and processes that message correctly.

First, for testing purpose, I will create a KafkaRunner that will produce a Kafka message on transaction_events_topic when the application starts:

````java
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaRunner implements CommandLineRunner {

    private final KafkaProducer kafkaProducer;

    @Override
    public void run(String... args) {
        log.info("Sending transactionEvent kafka message...");
        kafkaProducer.sendMessage(mockTransactionEvent());
    }

    private TransactionEvent mockTransactionEvent() {
        return TransactionEvent.newBuilder()
                .setHeader(TransactionEventHeader.newBuilder()
                        .setId(randomUUID().toString())
                        .setSourceSystem("kafka-demo-app")
                        .setCreatedAt(now(UTC).toEpochSecond())
                        .build()
                )
                .setBody(TransactionEventBody.newBuilder()
                        .setTransactionId(randomUUID().toString())
                        .setUserId(randomUUID().toString())
                        .setTransactionType(TransactionType.INSTANT_PAYMENT)
                        .setDate(now(UTC).toEpochSecond())
                        .setAmount(1500)
                        .setCurrency("EUR")
                        .setDescription("Birthday gift for John")
                        .build())
                .build();
    }
}
````

Last, I will check the logs from the terminal and see if the message was produced and consumed successfully.

