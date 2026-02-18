package com.accolite.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeadLetterMessage {

    private String rawRecord;
    private String errorMessage;
    private String fileName;
    private long recordIndex;
    private long timestamp;
    // getters/setters
}

