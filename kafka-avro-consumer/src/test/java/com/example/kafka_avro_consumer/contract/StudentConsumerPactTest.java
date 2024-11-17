package com.example.kafka_avro_consumer.contract;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.messaging.Message;
import au.com.dius.pact.core.model.messaging.MessagePact;
import com.arpangroup.model.Student;
import com.example.kafka_avro_consumer.consumer.StudentConsumer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(value = {PactConsumerTestExt.class, MockitoExtension.class})
@PactTestFor(providerName = "student-provider", providerType = ProviderType.ASYNCH, pactVersion = PactSpecVersion.V3)
public class StudentConsumerPactTest {

    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String KEY_CONTENT_TYPE = "contentType";

    @InjectMocks
    private StudentConsumer studentConsumer;

    @Pact(consumer = "student-consumer")
    public MessagePact studentDetailsPact(MessagePactBuilder builder) throws Exception {
        File file = ResourceUtils.getFile("src/test/resources/StudentResponse200.json");
        String content = new String(Files.readAllBytes(file.toPath()));

        /*PactDslJsonBody jsonBody = new PactDslJsonBody()
                .stringType("studentName", "John Doe")
                .stringType("studentId", "S12345")
                .integerType("age", 20);*/

        return builder
                .expectsToReceive("a student contract")
                //.withMetadata(Map.of(JSON_CONTENT_TYPE, KEY_CONTENT_TYPE))
                //.withContent(jsonBody)
                .withContent(content, "application/json")
                .toPact();
    }


/*    @Test
    @PactTestFor(pactMethod = "definePact", providerType = ProviderType.ASYNCH)
    void testStudentMessage(List<Message> messages) {
        String payload = new String(messages.get(0).getContents().getValue());

        // Assert the message payload matches the expected format (simplified check)
        assertEquals("{\"studentName\":\"John Doe\",\"studentId\":\"S12345\",\"age\":20}", payload);
    }*/

    /*@Test
    @PactTestFor(pactMethod = "studentDetailsPact", providerType = ProviderType.ASYNCH)
    void testStudentDetailsContract(List<Message> messages) throws Exception {
        // Convert the expected JSON to a JsonNode
        String expectedJson = "{\"studentName\":\"John Doe\",\"studentId\":\"S12345\",\"age\":20}";
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode expectedNode = objectMapper.readTree(expectedJson);

        // Get the payload from the message
        String payload = new String(messages.get(0).getContents().getValue());

        // Convert the actual payload to a JsonNode
        JsonNode actualNode = objectMapper.readTree(payload);

        // Compare the JSON nodes (this ignores the order of fields)
        assertEquals(expectedNode, actualNode);
    }*/

    @Test
    @PactTestFor(pactMethod = "studentDetailsPact", providerType = ProviderType.ASYNCH)
    void testStudentDetailsContract(List<Message> messages) throws Exception {
        // Convert the expected JSON to a JsonNode
        Student expectedStudent = new Student("John Doe", "S12345", 30);

        /*String payload = new String(messages.get(0).getContents().getValue());
        Student actualStudent = new ObjectMapper().readValue(payload, Student.class);
        assertThat(actualStudent).usingRecursiveComparison().isEqualTo(expectedStudent);*/

        messages.forEach(message -> {
            try {
                assert message.getContents().getValue() != null;
                String payload = new String(message.getContents().getValue()); // Get the payload from the message
                Student actualStudent =  new ObjectMapper().readValue(payload, Student.class);;
                assertThat(actualStudent).usingRecursiveComparison().isEqualTo(expectedStudent); // Compare the JSON nodes (this ignores the order of fields)
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
