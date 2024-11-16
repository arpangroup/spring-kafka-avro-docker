package com.example.kafka_avro_producer.util;

import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import com.arpangroup.model.Student;

import java.io.ByteArrayOutputStream;

public class AvroHelper {

    public static String serializeToJson(Student student) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        GenericDatumWriter<Student> writer = new GenericDatumWriter<>(student.getSchema());
        Encoder jsonEncoder = EncoderFactory.get().jsonEncoder(student.getSchema(), outputStream);
        writer.write(student, jsonEncoder);
        jsonEncoder.flush();
        return outputStream.toString();
    }
}
