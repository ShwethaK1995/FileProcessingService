package com.accolite.app.test;

import com.accolite.entity.ParsedRecord;
import com.accolite.kafka.producer.KafkaProducerService;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class KafkaProducerServiceTest {

    KafkaTemplate<Object, ParsedRecord> kafkaTemplate = mock(KafkaTemplate.class);

    KafkaProducerService service =
            new KafkaProducerService(kafkaTemplate);
    ParsedRecord parsedRecord= new ParsedRecord("123","ABC","testing","345",1.0,"N");

    @Test
    void shouldSendMessageToKafka() {

        verify(kafkaTemplate, times(1))
                .send(anyString(), eq(parsedRecord));
    }
    @Test
    void shouldThrowExceptionWhenKafkaFails() {


        doThrow(new RuntimeException("Kafka error"))
                .when(kafkaTemplate)
                .send(anyString(), any());

        KafkaProducerService service = new KafkaProducerService(kafkaTemplate);

        assertThrows(RuntimeException.class,
                () -> service.sendRecord(parsedRecord));
    }

}

