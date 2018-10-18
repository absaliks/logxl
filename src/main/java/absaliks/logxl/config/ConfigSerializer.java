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

import static absaliks.logxl.config.ConfigProperties.DATE_FROM;
import static absaliks.logxl.config.ConfigProperties.DATE_TO;
import static absaliks.logxl.config.ConfigProperties.FTP_DIRECTORY;
import static absaliks.logxl.config.ConfigProperties.FTP_LOGIN;
import static absaliks.logxl.config.ConfigProperties.FTP_PASSWORD;
import static absaliks.logxl.config.ConfigProperties.FTP_PORT;
import static absaliks.logxl.config.ConfigProperties.FTP_SERVER_NAME;
import static absaliks.logxl.config.ConfigProperties.LOGS_SOURCE;
import static absaliks.logxl.config.ConfigProperties.MASTER;
import static absaliks.logxl.config.ConfigProperties.PHONE;
import static absaliks.logxl.config.ConfigProperties.REPORT_TYPE;

import absaliks.logxl.log.LogsSource;
import absaliks.logxl.report.ReportType;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.logging.Level;
import lombok.extern.java.Log;

@Log
public class ConfigSerializer {

  private static final String CONFIG_FILE_PATH =
      System.getProperty("user.dir") + System.getProperty("file.separator") + "config.properties";

  public Config load() {
    if (!new File(CONFIG_FILE_PATH).exists()) {
      log.info("Конфигурационный файл не найден: " + CONFIG_FILE_PATH);
      throw new RuntimeException();
    }

    Properties properties = new Properties();
    try (InputStream input = new FileInputStream(CONFIG_FILE_PATH)) {
      properties.load(input);
      return mapPropertiesToConfig(properties);
    } catch (Exception e) {
      //TODO Make configuration optional
      log.log(Level.SEVERE, "Не удалось прочесть файл " + CONFIG_FILE_PATH, e);
      throw new RuntimeException(e);
    }
  }

  private Config mapPropertiesToConfig(Properties properties) {
    Config c = new Config();
    c.reportType = ReportType.valueOf(properties.getProperty(REPORT_TYPE));
    c.dateFrom = LocalDateTime.parse(properties.getProperty(DATE_FROM));
    c.dateTo = LocalDateTime.parse(properties.getProperty(DATE_TO));
    c.logsSource = LogsSource.valueOf(properties.getProperty(LOGS_SOURCE));

    c.ftpServer = properties.getProperty(FTP_SERVER_NAME);
    c.ftpPort = Integer.parseInt(properties.getProperty(FTP_PORT));
    c.ftpLogin = properties.getProperty(FTP_LOGIN);
    c.ftpPassword = properties.getProperty(FTP_PASSWORD);
    c.ftpDirectory = properties.getProperty(FTP_DIRECTORY);

    c.master = properties.getProperty(MASTER);
    c.phone = properties.getProperty(PHONE);
    return c;
  }
}

