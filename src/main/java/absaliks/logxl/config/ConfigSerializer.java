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

import static absaliks.logxl.config.ConfigProperties.*;

import absaliks.logxl.log.LogsSource;
import absaliks.logxl.report.ReportType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class ConfigSerializer {

  private static final Logger log = Logger.getLogger(ConfigSerializer.class.getName());

  private static final String PROPERTY_FILE_COMMENTS =
      "reportType:   {MINUTELY, HOURLY, DAILY}\n" +
          "date format:  1970-12-31T23:59:59";

  private static final String CONFIG_FILE_PATH =
      System.getProperty("user.dir") + System.getProperty("file.separator") + "config.properties";
  private static final int DEFAULT_FTP_PORT = 21;
  private static final LocalDate TODAY = LocalDate.now();

  public Config load() {
    if (!new File(CONFIG_FILE_PATH).exists()) {
      log.info("Конфигурационный файл не найден: " + CONFIG_FILE_PATH);
      return getDefaultConfig();
    }

    Properties properties = new Properties();
    try (InputStream stream = new FileInputStream(CONFIG_FILE_PATH)) {
      properties.load(stream);
      return mapPropertiesToConfig(properties);
    } catch (Exception e) {
      log.log(Level.WARNING, "Не удалось прочесть файл " + CONFIG_FILE_PATH, e);
      return getDefaultConfig();
    }
  }

  private Config getDefaultConfig() {
    Config config = new Config();
    config.dateFrom = LocalDateTime.of(TODAY, LocalTime.MIN);
    config.dateTo = LocalDateTime.of(TODAY, LocalTime.of(23, 59, 59));
    config.reportType = ReportType.DAILY;
    config.logsSource = LogsSource.FTP;
    return config;
  }

  private Config mapPropertiesToConfig(Properties properties) {
    Config c = getDefaultConfig();
    c.reportType = getEnum(ReportType.class, properties.getProperty(REPORT_TYPE), c.reportType);
    c.dateFrom = parseDateTime(properties.getProperty(DATE_FROM), c.dateFrom);
    c.dateTo = parseDateTime(properties.getProperty(DATE_TO), c.dateTo);
    c.logsSource = getEnum(LogsSource.class, properties.getProperty(LOGS_SOURCE), c.logsSource);
    c.localDirectory = properties.getProperty(LOCAL_DIRECTORY);
    c.ftpDirectory = properties.getProperty(FTP_DIRECTORY);
    c.ftpServer = properties.getProperty(FTP_SERVER_NAME);
    c.ftpPort = NumberUtils.toInt(properties.getProperty(FTP_PORT), DEFAULT_FTP_PORT);
    c.ftpLogin = properties.getProperty(FTP_LOGIN);
    c.ftpPassword = properties.getProperty(FTP_PASSWORD);
    c.savePassword = BooleanUtils.toBoolean(properties.getProperty(SAVE_PASSWORD));
    c.userName = properties.getProperty(USER_NAME);
    c.userPhone = properties.getProperty(USER_PHONE);

    c.isSilent = BooleanUtils.toBoolean(properties.getProperty(SILENT, "true"));
    return c;
  }

  private <E extends Enum<E>> E getEnum(final Class<E> enumClass, final String enumName,
      E defaultValue) {
    try {
      return Enum.valueOf(enumClass, enumName);
    } catch (final Exception e) {
      return defaultValue;
    }
  }

  private LocalDateTime parseDateTime(String text, LocalDateTime defaultValue) {
    try {
      return LocalDateTime.parse(text);
    } catch (Exception e) {
      return defaultValue;
    }
  }

  public void save(Config config) {
    try (OutputStream stream = new FileOutputStream(CONFIG_FILE_PATH)) {
      mapConfigToProperties(config).store(stream, PROPERTY_FILE_COMMENTS);
    } catch (Exception e) {
      log.log(Level.WARNING, "Не удалось сохранить файл " + CONFIG_FILE_PATH, e);
    }
  }

  private Properties mapConfigToProperties(Config config) {
    Properties properties = new NullSafeProperties();
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

