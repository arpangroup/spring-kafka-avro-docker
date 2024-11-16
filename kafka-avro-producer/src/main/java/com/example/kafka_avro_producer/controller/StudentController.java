package com.example.kafka_avro_producer.controller;

import com.arpangroup.model.Student;
import com.example.kafka_avro_producer.producer.StudentProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/")
@Slf4j
public class StudentController {
    @Autowired
    StudentProducer studentProducer;

    @Value("${kafka.topic.name}")
    private String kafkaTopicName;

    @GetMapping("/ping")
    public String ping() {
        return "Hello World!";
    }

    @PostMapping("/createStudent")
    public String getDataForKafkaTopic(@RequestBody Student student) {
        log.info("send data....to topicName {}-{}", kafkaTopicName, student.toString());
        studentProducer.sendMessage(kafkaTopicName, student);
        return "Sata Posted";
    }
}
