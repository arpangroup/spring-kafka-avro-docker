package com.example.kafka_avro_producer.controller;

import com.arpangroup.model.Order;
import com.arpangroup.model.OrderStatus;
import com.arpangroup.model.Student;
import com.example.kafka_avro_producer.service.KafkaProducerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/")
@Slf4j
public class KafkaProducerController {
    @Autowired
    KafkaProducerService kafkaProducerService;

    @Value("${kafka.topic.name}")
    private String kafkaTopicName;

    @GetMapping("/ping")
    public String ping() {
        return "Hello World!";
    }

    @GetMapping(value = "/order", produces = MediaType.APPLICATION_JSON_VALUE)
    public Order getOrder() {
        Order order = new Order();
        order.setOrderId("123");
        order.setItems(List.of("item1", "item2", "item3"));
        order.setStatus(OrderStatus.DELIVERED);
        order.setMetadata(Map.of("key", "value"));
        return order;
    }

    @PostMapping("/createStudent")
    public String getDataForKafkaTopic(@RequestBody Student student) {
        log.info("send data....to topicName {}-{}", kafkaTopicName, student.toString());
        kafkaProducerService.sendAvroData(kafkaTopicName, student);
        return "Sata Posted";
    }
}
