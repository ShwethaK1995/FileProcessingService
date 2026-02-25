package com.accolite.app.test;

import com.accolite.kafka.producer.KafkaProducerService;
import com.accolite.entity.DeadLetterMessage;
import com.accolite.entity.ParsedRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class KafkaProducerServiceTest {

    @Test
    void sendRecord_shouldCallKafkaTemplateSend() {
        KafkaTemplate<String, ParsedRecord> recordTemplate = mock(KafkaTemplate.class);
        KafkaTemplate<String, DeadLetterMessage> dltTemplate = mock(KafkaTemplate.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);

        when(recordTemplate.send(anyString(), anyString(), any(ParsedRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        KafkaProducerService svc = new KafkaProducerService(recordTemplate, dltTemplate,objectMapper);
        ReflectionTestUtils.setField(svc, "parserTopic", "test-topic");

        ParsedRecord record = new ParsedRecord("CUS123", "IN", "RELIANCEIN", "1234", 1235.56, "N");
        svc.sendRecord("CUS123", record, "input.txt", 5L);

        verify(recordTemplate).send(eq("test-topic"), eq("CUS123"), eq(record));
        verifyNoInteractions(dltTemplate); // because future completed successfully, no DLT path
    }

    @Test
    void sendDLT_shouldSendToDltTopic() {
        KafkaTemplate<String, ParsedRecord> recordTemplate = mock(KafkaTemplate.class);
        KafkaTemplate<String, DeadLetterMessage> dltTemplate = mock(KafkaTemplate.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);

        when(dltTemplate.send(anyString(), anyString(), any(DeadLetterMessage.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        KafkaProducerService svc = new KafkaProducerService(recordTemplate, dltTemplate,objectMapper);
        ReflectionTestUtils.setField(svc, "dltTopic", "parser-dlt-topic");

        DeadLetterMessage msg = new DeadLetterMessage();
        msg.setRawRecord("bad-msg");
        msg.setErrorMessage("ERR");
        msg.setFileName("input.txt");
        msg.setRecordIndex(1);
        msg.setTimestamp(System.currentTimeMillis());

        svc.sendDLT("CUS123", msg);

        verify(dltTemplate).send(eq("parser-dlt-topic"), eq("CUS123"), eq(msg));
    }
}