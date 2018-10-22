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
import static absaliks.logxl.config.ConfigProperties.LOCAL_DIRECTORY;
import static absaliks.logxl.config.ConfigProperties.LOGS_SOURCE;
import static absaliks.logxl.config.ConfigProperties.REPORT_TYPE;
import static absaliks.logxl.config.ConfigProperties.SAVE_PASSWORD;
import static absaliks.logxl.config.ConfigProperties.USER_NAME;
import static absaliks.logxl.config.ConfigProperties.USER_PHONE;

import absaliks.logxl.log.LogsSource;
import absaliks.logxl.report.ReportType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.logging.Level;
import lombok.extern.java.Log;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

@Log
public class ConfigSerializer {

  private static final String PROPERTY_FILE_COMMENTS =
      "reportType:   {MINUTELY, HOURLY, DAILY}\n" +
          "date format:  1970-12-31T23:59:59";

  private static final String CONFIG_FILE_PATH =
      System.getProperty("user.dir") + System.getProperty("file.separator") + "config.properties";
  public static final int DEFAULT_FTP_PORT = 21;

  public Config load() {
    if (!new File(CONFIG_FILE_PATH).exists()) {
      log.info("Конфигурационный файл не найден: " + CONFIG_FILE_PATH);
      return new Config();
    }

    Properties properties = new Properties();
    try (InputStream stream = new FileInputStream(CONFIG_FILE_PATH)) {
      properties.load(stream);
      return mapPropertiesToConfig(properties);
    } catch (Exception e) {
      log.log(Level.WARNING, "Не удалось прочесть файл " + CONFIG_FILE_PATH, e);
      return new Config();
    }
  }

  private Config mapPropertiesToConfig(Properties properties) {
    Config c = new Config();
    c.reportType = ReportType.valueOf(properties.getProperty(REPORT_TYPE));
    c.dateFrom = LocalDateTime.parse(properties.getProperty(DATE_FROM));
    c.dateTo = LocalDateTime.parse(properties.getProperty(DATE_TO));
    c.logsSource = LogsSource.valueOf(properties.getProperty(LOGS_SOURCE));

    c.localDirectory = properties.getProperty(LOCAL_DIRECTORY);
    c.ftpDirectory = properties.getProperty(FTP_DIRECTORY);

    c.ftpServer = properties.getProperty(FTP_SERVER_NAME);
    c.ftpPort = NumberUtils.toInt(properties.getProperty(FTP_PORT), DEFAULT_FTP_PORT);
    c.ftpLogin = properties.getProperty(FTP_LOGIN);
    c.ftpPassword = properties.getProperty(FTP_PASSWORD);

    c.savePassword = BooleanUtils.toBoolean(properties.getProperty(SAVE_PASSWORD));

    c.userName = properties.getProperty(USER_NAME);
    c.userPhone = properties.getProperty(USER_PHONE);
    return c;
  }

  public void save(Config config) {
    try (OutputStream stream = new FileOutputStream(CONFIG_FILE_PATH)) {
      val properties = mapConfigToProperties(config);
      properties.store(stream, PROPERTY_FILE_COMMENTS);
    } catch (Exception e) {
      log.log(Level.WARNING, "Не удалось сохранить файл " + CONFIG_FILE_PATH, e);
    }
  }

  private Properties mapConfigToProperties(Config config) {
    Properties properties = new Properties();
    properties.setProperty(REPORT_TYPE, config.reportType.name());
    properties.setProperty(DATE_FROM, config.dateFrom.toString());
    properties.setProperty(DATE_TO, config.dateTo.toString());
    properties.setProperty(LOGS_SOURCE, config.logsSource.name());

    properties.setProperty(LOCAL_DIRECTORY, config.localDirectory);
    properties.setProperty(FTP_DIRECTORY, config.ftpDirectory);

    properties.setProperty(FTP_SERVER_NAME, config.ftpServer);
    properties.setProperty(FTP_PORT, Integer.toString(config.ftpPort));
    properties.setProperty(FTP_LOGIN, config.ftpLogin);
    if (config.savePassword) {
      properties.setProperty(FTP_PASSWORD, config.ftpPassword);
    }

    properties.setProperty(SAVE_PASSWORD, Boolean.toString(config.savePassword));

    properties.setProperty(USER_NAME, config.userName);
    properties.setProperty(USER_PHONE, config.userPhone);
    return properties;
  }
}

