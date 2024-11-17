package com.example.kafka_avro_producer.contract;

import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import com.arpangroup.model.Student;
import com.example.kafka_avro_producer.producer.StudentProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@Provider("student-provider")
//@PactBroker(url = "http://localhost:9292")
@PactBroker(host = "localhost", port = "9292")
@SpringBootTest
public class StudentProviderPactTest {
    @Autowired
    private StudentProducer studentProducer;

    @BeforeEach
    void setup(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", 8080));
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    // Define the state for the interaction
    @State("a student created event")
    public void verifyStudentCreatedEvent() {
        Student student = Student.newBuilder()
                .setStudentName("John Doe")
                .setStudentId("S12345")
                .setAge(20)
                .build();

        studentProducer.sendMessage(student);
    }
}
