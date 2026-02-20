package com.accolite.app.test;

import com.accolite.entity.ParsedRecord;
import com.accolite.util.FileParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileParserTest {

    @Test
    void parseLine_validFixedWidth_shouldParseAllFields() {
        // 31 chars minimum
        // cusip(8) country(2) desc(10) isin(4) lotsize(6) indicator(1)
        String line = "123ABL89" + "US" + "RELIANCEIN" + "1234" + "123560" + "1";
        assertTrue(line.length() >= 31);

        ParsedRecord r = FileParser.parseLine(line);

        assertEquals("123ABL89", r.getCusipId());
        assertEquals("US", r.getCountryCode());
        assertEquals("RELIANCEIN", r.getDescription());
        assertEquals("1234", r.getIsin());
        assertEquals(1235.60, r.getLotSize(), 0.0001);
        assertEquals("Y", r.getIndicator());
    }

    @Test
    void parseLine_shortLine_shouldThrow() {
        String shortLine = "too short";

        StringIndexOutOfBoundsException ex =
                assertThrows(StringIndexOutOfBoundsException.class,
                        () -> FileParser.parseLine(shortLine));

        assertNotNull(ex.getMessage());
        assertFalse(ex.getMessage().isBlank());
    }
}
