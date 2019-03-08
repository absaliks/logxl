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
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

public abstract class ReportBuilder {

  @Getter
  protected final List<Record> records = new ArrayList<>();

  public static ReportBuilder create(ReportType reportType) {
    if (reportType == ReportType.STRAIGHT) {
      return new StraightReportBuilder();
    } else
      return new AvgReportBuilder(reportType);
  }

  public abstract void consume(List<Record> rawRecords);

  public void flush() {}
}
