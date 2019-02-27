/*
 * LogXL is a program that reads log files from FTP and exports in Excel
 * Copyright (C) 2019  Shamil Absalikov
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package absaliks.logxl.log;

import static absaliks.logxl.log.LogFileProperties.DECIMAL_SEPARATOR;
import static absaliks.logxl.log.LogFileProperties.TIMESTAMP_PATTERN;
import static absaliks.logxl.log.LogFileProperties.VALUE_SEPARATOR;
import static java.util.Objects.nonNull;

import absaliks.logxl.config.Config;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@RequiredArgsConstructor
public class LogParser {

  private static final Logger log = Logger.getLogger(LogParser.class.getName());

  private static final int AVG_DATE_LINE_SIZE = 140;
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter
      .ofPattern(TIMESTAMP_PATTERN)
      .withZone(ZoneId.systemDefault());
  private static final byte[] VALUE_FIELDS = new byte[] {
      1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
      11, 12, 13, 14, 15, 16, 17, 18, 19,     // -20
      21, 22, 23, 24, 25, 28, 26, 27, 29, 30, 31, 32
  };

  private static final int MIN_DATA_LINE_LENGTH = TIMESTAMP_PATTERN.length()
      + VALUE_FIELDS.length * 2;

  private final InputStream stream;
  private final Config config;
  private int approxLinesCount;
  private boolean isDataTableFound;

  public List<Record> parse() throws IOException {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
      approxLinesCount = stream.available() / AVG_DATE_LINE_SIZE;
      return parseDataTable(reader);
    }
  }

  private ArrayList<Record> parseDataTable(BufferedReader reader) throws IOException {
    ArrayList<Record> records = new ArrayList<>(approxLinesCount);
    String line;
    while (nonNull(line = reader.readLine())) {
      if (!isDataTableFound && !(isDataTableFound = isDataLine(line))) {
        continue;
      }
      Record rec = parseDataLine(line);
      if (rec != null) {
        records.add(rec);
      }
    }
    return records;
  }

  private boolean isDataLine(String line) {
    if (line.length() < MIN_DATA_LINE_LENGTH) {
      return false;
    }
    try {
      final String datetime = line.substring(0, TIMESTAMP_PATTERN.length());
      parseDateTime(datetime);
      return true;
    } catch (DateTimeParseException e) {
      return false;
    }
  }

  private Record parseDataLine(String line) {
    try {
      String[] fields = StringUtils.splitPreserveAllTokens(line, VALUE_SEPARATOR);
      LocalDateTime datetime = parseDateTime(fields[0]);
      if (datetime.isBefore(config.dateFrom) || datetime.isAfter(config.dateTo)) {
        log.log(Level.FINE, "Skipping line that outside of time period: {}", line);
        return null;
      }

      Record r = new Record();
      r.datetime = datetime;
      r.values = new float[VALUE_FIELDS.length];
      for (int i = 0; i < VALUE_FIELDS.length; i++) {
        int fieldIx = VALUE_FIELDS[i];
        r.values[i] = Float.parseFloat(fields[fieldIx].replace(DECIMAL_SEPARATOR, '.'));
      }
      r.isHeatingOn = Float.parseFloat(fields[33].replace(DECIMAL_SEPARATOR, '.')) != 0;
      return r;
    } catch (Exception e) {
      log.severe("Failed to parse line: " + line);
      if (!config.isSilent) {
        throw new RuntimeException("Не удалось интерпретировать строку: " + line);
      }
      return null;
    }
  }

  private LocalDateTime parseDateTime(String field) {
    return FORMATTER.parse(field, LocalDateTime::from);
  }
}
