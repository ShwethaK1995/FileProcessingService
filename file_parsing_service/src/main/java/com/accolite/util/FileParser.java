package com.accolite.util;

import com.accolite.entity.ParsedRecord;
import org.springframework.stereotype.Component;

@Component
public class FileParser {

    private static int recordLength = 0;

    public FileParser(FileIngestProperties props) {
        recordLength = props.recordLength(); // add to props
    }

    public static ParsedRecord parseLine(String line) {
        if (line.length() < recordLength) {
            throw new IllegalArgumentException("Line too short: " + line);
        }

        String cusipId = line.substring(0, 8).trim();
        String countryCode = line.substring(8, 10).trim();
        String description = line.substring(10, 20).trim();
        String isin = line.substring(20, 24).trim();
        double lotSize = Double.parseDouble(line.substring(24, 30)) / 100;
        String indicator = line.substring(30, 31).equals("1") ? "Y" : "N";

        return new ParsedRecord(cusipId, countryCode, description, isin, lotSize, indicator);
    }
}
