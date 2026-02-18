package com.accolite.data_validation_service.service;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class ReferenceMessageTest {

    @Test
    void gettersAndSetters_shouldWork() {
        ReferenceMessage m = new ReferenceMessage();
        m.setCusipId("CUS123");
        m.setCountryCode("US");
        m.setDescription("DESC");
        m.setIsin("ISIN123");
        m.setLotSize(BigDecimal.TEN);
        m.setAction("I");

        assertEquals("CUS123", m.getCusipId());
        assertEquals("US", m.getCountryCode());
        assertEquals("DESC", m.getDescription());
        assertEquals("ISIN123", m.getIsin());
        assertEquals(BigDecimal.TEN, m.getLotSize());
        assertEquals("I", m.getAction());
    }

    @Test
    void equalsAndToString_shouldWork() {
        ReferenceMessage m1 = new ReferenceMessage("CUS123","US","DESC","ISIN123",BigDecimal.TEN,"I");
        ReferenceMessage m2 = new ReferenceMessage("CUS123","US","DESC","ISIN123",BigDecimal.TEN,"I");

        assertEquals(m1, m2);
        assertTrue(m1.toString().contains("CUS123"));
    }
}
