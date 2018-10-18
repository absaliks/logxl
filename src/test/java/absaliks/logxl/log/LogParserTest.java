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

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
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
    InputStream stream = new ByteArrayInputStream(GIVEN_CSV.getBytes());
    List<Record> actualRecords = new LogParser(stream).parse();
    assertEquals(createExpectedRecordList(), actualRecords);
  }

  private List<Record> createExpectedRecordList() {
    Record rec1 = new Record();
    rec1.datetime = LocalDateTime.of(2018, 2, 14, 9, 5, 15);
    rec1.values = new float[]{3.5f, -2.96f, 24, -25, -24.98f, -24, -25.4f, -25, 44,
        1, 2, 3, 4, 5, 6, 7, 8, 9, 0, -1, -2, -3, -4, -5, -6, -7, -8, -9, -10, 10, -11, 12, 15.55f};

    Record rec2 = new Record();
    rec2.datetime = LocalDateTime.of(2018, 6, 2, 12, 2, 7);
    rec2.values = new float[]{-16, -15, -14, -13, -12, -11, -10, -9, -8, -7, -6, -5, -4, -3,
        -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
    return asList(rec1, rec2);
  }
}