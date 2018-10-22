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
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;

@Log
public class LogParser {

  private static final int AVG_DATE_LINE_SIZE = 140;
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter
      .ofPattern("yyyy.MM.dd_HH:mm:ss")
      .withZone(ZoneId.systemDefault());

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
      r.values = new float[33];
      for (int i = 1; i < fields.length; i++) {
        r.values[i - 1] = Float.parseFloat(fields[i].replace(',', '.'));
      }
      return r;
    } catch (Exception e) {
      log.severe("Failed to parse line: " + line);
      throw new RuntimeException("Ошибка обработки строки: " + line);
    }
  }

  private void skipFileHeader(BufferedReader reader) throws IOException {
    String line;
    do {
      line = reader.readLine();
      if (line == null) {
        throw new IllegalStateException("Table header not found");
      }
    } while (!line.startsWith("Timestamp;"));
  }
}
