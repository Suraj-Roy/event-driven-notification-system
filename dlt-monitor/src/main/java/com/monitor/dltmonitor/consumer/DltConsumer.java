package com.monitor.dltmonitor.consumer;

import com.monitor.dltmonitor.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DltConsumer {

    private final AlertService alertService;




    @KafkaListener(
            topics = {
                    "notification-email.DLT",
                    "notification-push.DLT",
                    "notification-request.DLT"
            },
            groupId = "dlt-monitor-group"
    )
    public void consume(ConsumerRecord<String, String> record, Acknowledgment ack) {
        log.error("DLT message received: topic={}, key={}, partition={}, offset={}",
                record.topic(), record.key(),
                record.partition(), record.offset());

        String originalTopic = record.topic().replace(".DLT", "");
        String alertMessage = String.format(
                "FAILED MESSAGE\nOriginal Topic: %s\nKey: %s\nValue: %s",
                originalTopic, record.key(), record.value()
        );

        alertService.sendAlert(alertMessage);
        ack.acknowledge();
    }
}