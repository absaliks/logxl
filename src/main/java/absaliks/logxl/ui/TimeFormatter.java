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

package absaliks.logxl.ui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

@Log
public class TimeFormatter {

  private static final DateTimeFormatter LONG_TIME_FORMATTER = DateTimeFormatter
      .ofPattern("HH:mm:ss");
  private static final DateTimeFormatter SHORT_TIME_FORMATTER = DateTimeFormatter
      .ofPattern("HH:mm");

  public String format(LocalTime time) {
    if (time == null) {
      return null;
    }
    if (time.getSecond() == 0) {
      return SHORT_TIME_FORMATTER.format(time);
    }
    return LONG_TIME_FORMATTER.format(time);
  }

  public LocalDateTime buildDate(LocalDate date, String time) {
    String[] timeparts = StringUtils.split(time.replaceAll("[^0-9:.]", ""), ":.");
    int nano = 0;
    byte second = 0, minute = 0, hour = 0;
    switch (timeparts.length) {
      case 4:
        nano = NumberUtils.toInt(timeparts[3]);
      case 3:
        second = NumberUtils.toByte(timeparts[2]);
      case 2:
        minute = NumberUtils.toByte(timeparts[1]);
      case 1:
        hour = NumberUtils.toByte(timeparts[0]);
        break;
      default:
        log.warning("Unable to parse time from " + time);
    }
    return LocalDateTime.of(date, LocalTime.of(hour, minute, second, nano));
  }
}
