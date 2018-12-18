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

import absaliks.logxl.AbstractFactory;
import absaliks.logxl.config.Config;
import absaliks.logxl.filesource.LogFileSource;
import absaliks.logxl.log.LogParser;
import absaliks.logxl.log.Record;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.apache.commons.lang3.Validate;

public class ReportService {

  private static final Logger log = Logger.getLogger(ReportService.class.getName());
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter
      .ofPattern("yyyy.MM.dd_HH_mm")
      .withZone(ZoneId.systemDefault());

  private final DoubleProperty progress = new SimpleDoubleProperty();
  private final AbstractFactory factory;
  private final Config config;

  public ReportService(AbstractFactory factory) {
    this.config = factory.getConfig();
    this.factory = factory;
  }

  public DoubleProperty progress() {
    return progress;
  }

  public void createReport() throws IOException {
    resetProgress();
    final LogFileSource fileSource = factory.createLogFileSource();
    validateConfiguration();
    ReportExporter.deleteReportFile();
    try {
      fileSource.initialize();

      List<String> fileList = filterFileList(fileSource.getFileList());
      Validate.isTrue(!fileList.isEmpty(),
          "Не найдено ни одного файла удовлетворяющего выбранным датам");
      final int filesCount = fileList.size();

      final ReportBuilder builder = new ReportBuilder(config.reportType);
      for (int i = 0; i < fileList.size(); i++) {
        String filename = fileList.get(i);
        log.info("Обработка файла " + filename);
        File logFile = fileSource.getFile(filename);
        try (InputStream stream = new FileInputStream(logFile)) {
          List<Record> records = new LogParser(stream, config.dateFrom, config.dateTo).parse();
          builder.consume(records);
        } catch (Exception e) {
          log.log(Level.SEVERE, "Parse error in file " + filename, e);
          throw new RuntimeException(
              "Ошибка при обработке файла " + filename + ":\n" + e.getMessage());
        }
        progress.setValue((0.0 + i) / filesCount);
      }

      new ReportExporter(config).export(builder.flush());
      progress.setValue(1);
    } catch (Exception e) {
      fileSource.destroy();
      throw e;
    }
  }

  private void resetProgress() {
    progress.setValue(0);
  }

  private void validateConfiguration() {
    LocalDateTime dateFrom = config.dateFrom;
    LocalDateTime dateTo = config.dateTo;
    Validate.notNull(dateFrom, "Параметр дата начала (dateFrom) не установлен");
    Validate.notNull(dateTo, "Параметр дата начала (dateTo) не установлен");
    Validate.isTrue(dateTo.isAfter(dateFrom), "Дата начала должна быть до даты конца");
  }

  private List<String> filterFileList(List<String> fileList) {
    Validate.isTrue(!fileList.isEmpty(), "Нет файлов в выбранной папке");
    String from = FORMATTER.format(config.dateFrom) + ".csv";
    String to = FORMATTER.format(config.dateTo) + ".csv";
    return fileList.stream()
        .filter(name -> name.compareToIgnoreCase(from) >= 0 && name.compareToIgnoreCase(to) <= 0)
        .collect(Collectors.toList());
  }
}
