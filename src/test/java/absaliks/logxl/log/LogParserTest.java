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

import static absaliks.logxl.log.LogFileProperties.COLUMNS_COUNT;
import static absaliks.logxl.log.LogFileProperties.COLUMNS_SEPARATOR;
import static absaliks.logxl.log.LogParser.FORMATTER;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import absaliks.logxl.config.Config;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class LogParserTest {

  private static final String DATATABLE_HEADER = "Timestamp;";
  private static final String GIVEN_CSV = String.join(System.lineSeparator(), asList(
      DATATABLE_HEADER,
      "2018.02.14_12:05:15;3,5;-2,96;24,00;-25;-24,98;-24,00;-25.4;-25;44;1;2;3;4;5;6;7;8;9;0;-1;-2;-3;-4;-5;-6;-7;-8;-9;-10;10;-11;12;15.55",
      "2018.06.02_15:02:07;-16;-15;-14;-13;-12;-11;-10;-9;-8;-7;-6;-5;-4;-3;-2;-1;000;1;2;3;4;5;6;7;8;9;10;11;12;13;14;15;16"
  ));

  @Test
  void parse() throws IOException {
    // FIXME
    assertEquals(createExpectedRecordList(), parse(GIVEN_CSV));
  }

  private List<Record> createExpectedRecordList() {
    Record rec1 = new Record();
    rec1.datetime = LocalDateTime.of(2018, 2, 14, 12, 5, 15);
    rec1.values = new float[]{3.5f, -2.96f, 24, -25, -24.98f, -24, -25.4f, -25, 44,
        1, 2, 3, 4, 5, 6, 7, 8, 9, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, 10, -11, 12, 15.55f};

    Record rec2 = new Record();
    rec2.datetime = LocalDateTime.of(2018, 6, 2, 15, 2, 7);
    rec2.values = new float[]{-16, -15, -14, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3,
        -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
    return asList(rec1, rec2);
  }

  private Config givenConfig() {
    val config = new Config();
    config.dateFrom = LocalDateTime.MIN;
    config.dateTo = LocalDateTime.MAX;
    return config;
  }

  @Test
  void testColumnOrder() throws IOException {
    val expectedTimestamp = LocalDateTime.now();
    final StringBuilder csvBuilder = new StringBuilder();
    csvBuilder.append(FORMATTER.format(expectedTimestamp));
    for (int i = 1; i < COLUMNS_COUNT; i++) {
      csvBuilder.append(COLUMNS_SEPARATOR).append(i);
    }

    Record expectedRecord = new Record();
    expectedRecord.datetime = expectedTimestamp.truncatedTo(ChronoUnit.SECONDS);
    expectedRecord.values = new float[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17,
        18, 19, 21, 22, 23, 24, 25, 28, 26, 27, 29, 30, 31, 32};
    expectedRecord.isHeatingOn = true;

    assertEquals(singletonList(expectedRecord), parse(csvBuilder.toString()));
  }

  private List<Record> parse(String givenCsv) throws IOException {
    InputStream stream = new ByteArrayInputStream(givenCsv.getBytes());
    return new LogParser(stream, givenConfig()).parse();
  }
}