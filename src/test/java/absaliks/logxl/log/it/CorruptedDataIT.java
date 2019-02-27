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

package absaliks.logxl.log.it;

import static absaliks.logxl.log.TestUtils.getResourceAsStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import absaliks.logxl.config.Config;
import absaliks.logxl.log.LogParser;
import absaliks.logxl.log.Record;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

public class CorruptedDataIT {

  private static final String CORRUPTED_TABLE_LOGFILE = "corrupted-data/corrupted-table.csv";
  private static final String MISSING_HEADER_LOGFILE = "corrupted-data/missing-header.csv";

  static {
    System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT %4$s: %5$s %6$s%n");
  }

  @Test
  void corruptedTable_silent_ignoresParsingErrors() throws IOException {
    try (val stream = getResourceAsStream(CORRUPTED_TABLE_LOGFILE)) {
      List<Record> records = new LogParser(stream, givenConfig(true)).parse();
      assertEquals(2, records.size());
      assertEquals(LocalDateTime.of(2018, 12, 13, 18, 46, 56), records.get(0).datetime);
      assertEquals(LocalDateTime.of(2018, 12, 13, 18, 46, 57), records.get(1).datetime);
    }
  }

  @Test
  void corruptedTable_nonSilent_throwsRuntimeException() throws IOException {
    try (val stream = getResourceAsStream(CORRUPTED_TABLE_LOGFILE)) {
      LogParser parser = new LogParser(stream, givenConfig(false));
      assertThrows(RuntimeException.class, parser::parse);
    }
  }

  @Test()
  void missingHeader_doesNotAffectAnything() throws IOException {
    try (val stream = getResourceAsStream(MISSING_HEADER_LOGFILE)) {
      List<Record> records = new LogParser(stream, givenConfig(true)).parse();
      assertEquals(1, records.size());
      assertEquals(LocalDateTime.of(2018, 12, 13, 18, 46, 57), records.get(0).datetime);
    }
  }

  private Config givenConfig(boolean isSilent) {
    val config = new Config();
    config.dateFrom = LocalDateTime.MIN;
    config.dateTo = LocalDateTime.MAX;
    config.isSilent = isSilent;
    return config;
  }
}
