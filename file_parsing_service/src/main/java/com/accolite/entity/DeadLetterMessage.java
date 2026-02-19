package com.accolite.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeadLetterMessage {

    private String key;

    // What failed: PARSE_ERROR, PUBLISH_FAILED, BAD_RECORD_LENGTH, etc.
    private String reason;

    // Original line (or truncated) so you can replay/debug
    private String rawRecord;

    // Error details (exception message)
    private String errorMessage;

    // Source context
    private String fileName;
    private long recordIndex;

    private long timestamp = System.currentTimeMillis();

    public static DeadLetterMessage of(
            String rawRecord,
            String errorMessage,
            String fileName,
            long recordIndex
    ) {
        DeadLetterMessage m = new DeadLetterMessage();
        m.setRawRecord(rawRecord);
        m.setErrorMessage(errorMessage);
        m.setFileName(fileName);
        m.setRecordIndex(recordIndex);
        m.setTimestamp(System.currentTimeMillis());
        return m;
    }

}


