package com.example.kafka_avro_producer.contract;

import au.com.dius.pact.provider.MessageAndMetadata;
import au.com.dius.pact.provider.PactVerifyProvider;
import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.MessageTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Consumer;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.junitsupport.loader.PactUrl;
import com.arpangroup.model.Student;
import com.example.kafka_avro_producer.producer.StudentProducer;
import com.example.kafka_avro_producer.util.AvroHelper;
import com.example.kafka_avro_producer.util.AvroUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;


@Provider("student-provider")
//@PactBroker(url = "http://localhost:9292")
@PactFolder("src/test/resources/pacts/")
@SpringBootTest
@Slf4j
public class StudentProviderPactTest {
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String KEY_CONTENT_TYPE = "contentType";
    private static final String AVRO_CONTENT_TYPE = "application/avro";

    @BeforeEach
    void setup(PactVerificationContext context) {
        System.out.println("Setting up the provider test context...");
        // context.setTarget(new HttpTestTarget("localhost", 8080));
        context.setTarget(new MessageTestTarget());
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    /*@PactVerifyProvider("a student contract")
    public MessageAndMetadata verifyMessage() throws Exception {
        System.out.println("Verifying message contract...");

        Student student = new Student("John Doe", "S12345", 30);
        byte[] avroData = AvroHelper.serializeToAvro(student);
        Map<String, String> metadata = Map.of("contentType", "application/avro");

        System.out.println("Returning Avro Data: " + Arrays.toString(avroData));

        return new MessageAndMetadata(avroData, metadata);
    }*/

/*    @PactVerifyProvider("a student contract")
    public MessageAndMetadata verifyMessage() {
        System.out.println("Verifying message contract...");
        String jsonMessage = "{ \"age\": 30, \"studentId\": \"S12345\", \"studentName\": \"John Doe\" }";

        // Set metadata for content type as JSON
        Map<String, String> metadata = Map.of("contentType", "application/json");

        // Return the message with the correct metadata
        return new MessageAndMetadata(jsonMessage.getBytes(StandardCharsets.UTF_8), metadata);
    }*/

    @PactVerifyProvider("a student contract")
    public MessageAndMetadata verifyMessage() throws Exception {
        System.out.println("Verifying message contract...");

        // Create a Student object
        Student student = new Student("John Doe", "S12345", 30);

        // Serialize the Avro object to JSON format
       /* ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DatumWriter<SpecificRecordBase> writer = new SpecificDatumWriter<>(student.getSchema());
        JsonEncoder jsonEncoder = EncoderFactory.get().jsonEncoder(student.getSchema(), outputStream);
        writer.write(student, jsonEncoder);
        jsonEncoder.flush();
        String jsonMessage = outputStream.toString(StandardCharsets.UTF_8);*/

        String jsonMessage = AvroUtils.convertAvroToJson(student);
        Map<String, String> metadata = Map.of("contentType", "application/json");

        System.out.println("Generated JSON Message: " + jsonMessage);

        // Return the message with the correct metadata
        return new MessageAndMetadata(jsonMessage.getBytes(StandardCharsets.UTF_8), metadata);
    }


    /*@State("a student created event")
    public void verifyStudentCreatedEvent() {
        Student student = Student.newBuilder()
                .setStudentName("John Doe")
                .setStudentId("S12345")
                .setAge(20)
                .build();

        studentProducer.sendMessage(student);
    }*/



    @State("a student created event")
    public void setupStudent() {
        // Prepare the state for the test, like sending a message to the provider
    }
}
