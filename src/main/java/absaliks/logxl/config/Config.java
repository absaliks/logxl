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

package absaliks.logxl.config;

import absaliks.logxl.log.LogsSource;
import absaliks.logxl.report.ReportType;
import java.time.LocalDateTime;
import lombok.ToString;

@ToString(exclude = "ftpPassword")
public class Config {

  public ReportType reportType;
  public LocalDateTime dateFrom;
  public LocalDateTime dateTo;
  public LogsSource logsSource;

  public String ftpServer;
  public int ftpPort;
  public String ftpLogin;
  public String ftpPassword;
  public String ftpDirectory;

  public String master;
  public String phone;
}
