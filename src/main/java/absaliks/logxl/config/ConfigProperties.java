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

interface ConfigProperties {
  String REPORT_TYPE = "reportType";
  String DATE_FROM = "dateFrom";
  String DATE_TO = "dateTo";
  String LOGS_SOURCE = "logsSource";

  String FTP_DIRECTORY = "ftpDirectory";
  String LOCAL_DIRECTORY = "localDirectory";

  String FTP_SERVER_NAME = "ftpServerName";
  String FTP_PORT = "ftpPort";
  String FTP_LOGIN = "ftpLogin";
  String FTP_PASSWORD = "ftpPassword";

  String SAVE_PASSWORD = "savePassword";

  String USER_NAME = "userName";
  String USER_PHONE = "userPhone";

  String SILENT = "silent";
}
