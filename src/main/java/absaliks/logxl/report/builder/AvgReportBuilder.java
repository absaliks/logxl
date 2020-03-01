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

package absaliks.logxl.report.builder;

import absaliks.logxl.log.Record;
import absaliks.logxl.report.ReportType;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AvgReportBuilder extends ReportBuilder {

  private final ReportType reportType;
  // TODO: set initial size
  private List<Record> buffer;

  private LocalDateTime nextCutOffDateTime;

  public AvgReportBuilder(ReportType reportType) {
    this.reportType = reportType;
    initBuffer();
  }

  private void initBuffer() {
    this.buffer = new ArrayList<>(getInitialBufferSize());
  }

  private int getInitialBufferSize() {
    switch (reportType) {
      case MINUTELY:
        return 60;
      case HOURLY:
        return 60 * 60;
      case DAILY:
        return 24 * 60 * 60;
      default:
        return 16;
    }
  }

  public void consume(List<Record> rawRecords) {
    if (!rawRecords.isEmpty()) {
      if (nextCutOffDateTime == null) {
        nextCutOffDateTime = calcNextCutOffTimeOf(rawRecords.get(0).datetime);
      }
      rawRecords.forEach(rec -> {
        if (!rec.datetime.isBefore(nextCutOffDateTime)) {
          nextCutOffDateTime = calcNextCutOffTimeOf(rec.datetime); // TODO: is there better way?
          records.add(createReportRecord());
          initBuffer();
        }
        buffer.add(rec);
      });
    }
  }

  private LocalDateTime calcNextCutOffTimeOf(LocalDateTime firstRecord) {
    return truncateTime(firstRecord).plus(1, reportType.relatedTemporalUnit);
  }

  private LocalDateTime truncateTime(LocalDateTime firstRecord) {
    int minute = firstRecord.getMinute(),
        hour = firstRecord.getHour();
    switch (reportType) {
      case DAILY:
        hour = 0;
      case HOURLY:
        minute = 0;
    }
    return LocalDateTime.of(firstRecord.toLocalDate(), LocalTime.of(hour, minute));
  }

  private Record createReportRecord() {
    final Record result = new Record();
    result.datetime = truncateTime(buffer.get(0).datetime);
    setValues(result);
    return result;
  }

  private void setValues(final Record result) {
    final int valuesCount = buffer.get(0).values.length;
    result.values = new float[valuesCount];
    for (Record rec : buffer) {
      for (int i = 0; i < valuesCount; i++) {
        result.values[i] += rec.values[i];
      }
      if (rec.isHeatingCableOn) {
        result.isHeatingCableOn = true;
      }
      if (rec.isHeatingElementOn) {
        result.isHeatingElementOn = true;
      }
    }
    for (int i = 0; i < valuesCount; i++) {
      result.values[i] /= buffer.size();
    }
  }

  public void flush() {
    if (!buffer.isEmpty()) {
      records.add(createReportRecord());
    }
  }
}
