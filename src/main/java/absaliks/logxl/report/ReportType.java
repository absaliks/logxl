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

public enum ReportType {
  MINUTELY("Поминутный", ChronoUnit.MINUTES, ChronoField.MINUTE_OF_HOUR),
  HOURLY("Часовой", ChronoUnit.HOURS, ChronoField.HOUR_OF_DAY),
  DAILY("Суточный", ChronoUnit.DAYS, ChronoField.DAY_OF_MONTH);

  public final String description;
  public final TemporalUnit relatedTemporalUnit;
  public final ChronoField relatedChronoField;

  ReportType(String description, TemporalUnit relatedTemporalUnit,
      ChronoField relatedChronoField) {
    this.description = description;
    this.relatedTemporalUnit = relatedTemporalUnit;
    this.relatedChronoField = relatedChronoField;
  }

  @Override
  public String toString() {
    return description;
  }
}
