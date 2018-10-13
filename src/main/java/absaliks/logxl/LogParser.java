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

package absaliks.logxl;

import static java.util.Objects.nonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class LogParser {

  private static final int AVG_DATE_LINE_SIZE = 150;
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter
      .ofPattern("yyyy.MM.dd_HH:mm:ss")
      .withZone(ZoneId.systemDefault());

  private InputStream stream;
  private int approxLinesCount;

  public LogParser(InputStream stream) {
    this.stream = stream;
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
      records.add(parseDataLine(line));
    }
    return records;
  }

  private Record parseDataLine(String line) {
    String[] fields = StringUtils.split(line, ';');
    Record r = new Record();
    r.datetime = FORMATTER.parse(fields[0], Instant::from);
    r.temperature = new float[33];
    for (int i = 1; i < fields.length; i++) {
      r.temperature[i - 1] = Float.parseFloat(fields[i].replace(',', '.'));
    }
    return r;
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
