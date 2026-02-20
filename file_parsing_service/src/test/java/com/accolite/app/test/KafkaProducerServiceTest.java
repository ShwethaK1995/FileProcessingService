package com.accolite.app.test;

import com.accolite.entity.ParsedRecord;
import com.accolite.entity.DeadLetterMessage;
import com.accolite.kafka.producer.KafkaProducerService;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

class KafkaProducerServiceTest {

    @Test
    void sendRecord_shouldCallKafkaTemplateSend() {
        KafkaTemplate<String, ParsedRecord> recordTemplate = mock(KafkaTemplate.class);
        KafkaTemplate<String, DeadLetterMessage> dltTemplate = mock(KafkaTemplate.class);

        KafkaProducerService svc = new KafkaProducerService(recordTemplate, dltTemplate);

        // IMPORTANT: set correct field name exactly as in your service
        ReflectionTestUtils.setField(svc, "parserTopic", "parser-topic");
        ReflectionTestUtils.setField(svc, "dltTopic", "dlt-topic");

        when(recordTemplate.send(anyString(), anyString(), any(ParsedRecord.class)))
                .thenReturn(java.util.concurrent.CompletableFuture.completedFuture(null));

        ParsedRecord r = new ParsedRecord("CUS", "US", "DESC", "ISIN", 1.0, "Y");
        svc.sendRecord("k1",r,"test.txt",0);

        verify(recordTemplate, times(1))
                .send(eq("parser-topic"), eq("k1"), eq(r));
    }


    @Test
    void sendDLT_shouldSendToDltTopic() {
        KafkaTemplate<String, ParsedRecord> recordTemplate = mock(KafkaTemplate.class);
        KafkaTemplate<String, DeadLetterMessage> dltTemplate = mock(KafkaTemplate.class);

        KafkaProducerService svc = new KafkaProducerService(recordTemplate, dltTemplate);
        ReflectionTestUtils.setField(svc, "parserTopic", "parser-topic");
        ReflectionTestUtils.setField(svc, "dltTopic", "dlt-topic");

        when(dltTemplate.send(eq("dlt-topic"), eq("k2"), any(DeadLetterMessage.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        DeadLetterMessage msg = new DeadLetterMessage();

        svc.sendDLT("k2", msg);

        verify(dltTemplate, times(1)).send("dlt-topic", "k2", msg);
    }

}

