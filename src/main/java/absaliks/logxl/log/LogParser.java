/*
 * LogXL is a program that reads log files from FTP and exports in Excel
 * Copyright (C) 2018  Shamil Absalikov
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

import static java.util.Objects.nonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

public class LogParser {

  private static final Logger log = Logger.getLogger(LogParser.class.getName());

  private static final int AVG_DATE_LINE_SIZE = 140;
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter
      .ofPattern("yyyy.MM.dd_HH:mm:ss")
      .withZone(ZoneId.systemDefault());
  private static final byte[] VALUE_FIELDS = new byte[]{
      1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
      11, 12, 13, 14, 15, 16, 17, 18, 19,     // -20
      21, 22, 23, 24, 25, 26, 27, 29, 30, // -28
      31, 32
  };

  private final InputStream stream;
  private final LocalDateTime from;
  private final LocalDateTime to;
  private int approxLinesCount;

  public LogParser(InputStream stream, LocalDateTime from, LocalDateTime to) {
    this.stream = stream;
    this.from = from;
    this.to = to;
  }

  public List<Record> parse() throws IOException {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
      approxLinesCount = stream.available() / AVG_DATE_LINE_SIZE;
      skipFileHeader(reader);
      return parseDataTable(reader);
    }
  }

  private ArrayList<Record> parseDataTable(BufferedReader reader) throws IOException {
    ArrayList<Record> records = new ArrayList<>(approxLinesCount);
    String line;
    while (nonNull(line = reader.readLine())) {
      Record rec = parseDataLine(line);
      if (rec != null) {
        records.add(rec);
      }
    }
    return records;
  }

  private Record parseDataLine(String line) {
    try {
      String[] fields = StringUtils.split(line, ';');
      LocalDateTime datetime = FORMATTER.parse(fields[0], LocalDateTime::from);
      if (datetime.isBefore(from) || datetime.isAfter(to)) {
        log.log(Level.FINE, "Skipping line that outside of time period: {}", line);
        return null;
      }

      Record r = new Record();
      r.datetime = datetime;
      r.values = new float[VALUE_FIELDS.length];
      for (int i = 0; i < VALUE_FIELDS.length; i++) {
        int fieldIx = VALUE_FIELDS[i];
        r.values[i] = Float.parseFloat(fields[fieldIx].replace(',', '.'));
      }
      r.isHeatingOn = Float.parseFloat(fields[33].replace(',', '.')) != 0;
      return r;
    } catch (Exception e) {
      log.severe("Failed to parse line: " + line);
      throw new RuntimeException("Не удалось интерпретировать строку: " + line);
    }
  }

  private void skipFileHeader(BufferedReader reader) throws IOException {
    String line;
    do {
      line = reader.readLine();
      if (line == null) {
        throw new IllegalStateException("Не найден заголовок файла");
      }
    } while (!line.startsWith("Timestamp;"));
  }
}
