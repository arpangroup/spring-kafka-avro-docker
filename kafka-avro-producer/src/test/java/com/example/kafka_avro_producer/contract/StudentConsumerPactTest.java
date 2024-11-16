package com.example.kafka_avro_producer.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import au.com.dius.pact.consumer.MessagePactBuilder;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.consumer.junit5.ProviderType;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.messaging.Message;
import au.com.dius.pact.core.model.messaging.MessagePact;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "student-provider", pactMethod = "definePact", providerType = ProviderType.ASYNCH, pactVersion = PactSpecVersion.V3)
public class StudentConsumerPactTest {

    @Pact(consumer = "student-consumer", provider = "student-provider")
    public MessagePact definePact(MessagePactBuilder builder) {
        PactDslJsonBody content = new PactDslJsonBody()
                .stringType("studentName", "John Doe")
                .stringType("studentId", "S12345")
                .integerType("age", 20);

        return builder
                .expectsToReceive("a student created event")
                //.withMetadata(Map.of("contentType", "application/json"))
                .withContent(content)
                .toPact();
    }


/*    @Test
    @PactTestFor(pactMethod = "definePact", providerType = ProviderType.ASYNCH)
    void testStudentMessage(List<Message> messages) {
        String payload = new String(messages.get(0).getContents().getValue());

        // Assert the message payload matches the expected format (simplified check)
        assertEquals("{\"studentName\":\"John Doe\",\"studentId\":\"S12345\",\"age\":20}", payload);
    }*/

    @Test
    @PactTestFor(pactMethod = "definePact", providerType = ProviderType.ASYNCH)
    void testStudentMessage(List<Message> messages) throws Exception {
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
    }
}
