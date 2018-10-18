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

package absaliks.logxl.report;

import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ReportType {
  MINUTELY(ChronoUnit.MINUTES, ChronoField.MINUTE_OF_HOUR),
  HOURLY(ChronoUnit.HOURS, ChronoField.HOUR_OF_DAY),
  DAILY(ChronoUnit.DAYS, ChronoField.DAY_OF_MONTH);

  public final TemporalUnit relatedTemporalUnit;
  public final ChronoField relatedChronoField;
}
