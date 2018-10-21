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

import absaliks.logxl.config.Config;
import absaliks.logxl.log.LogParser;
import absaliks.logxl.log.Record;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.apache.commons.lang3.Validate;

@Log
@RequiredArgsConstructor
public class ReportService {

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter
      .ofPattern("yyyy.MM.dd_HH_mm_ss")
      .withZone(ZoneId.systemDefault());

  private final Config config;
  private final LogFileSource fileSource;

  @SneakyThrows
  public void createReport() {
    validateConfiguration();
    ReportExporter.deleteReportFile();
    try {
      fileSource.initialize();

      List<String> fileList = createFileList();
      Validate.isTrue(!fileList.isEmpty(),
          "Не найдено ни одного файла удовлетворяющего выбранным датам");

      ReportBuilder builder = new ReportBuilder(config.reportType);
      for (String filename : fileList) {
        log.info("Обработка файла " + filename);
        File logFile = fileSource.getFile(filename);
        try (InputStream stream = new FileInputStream(logFile)) {
          List<Record> records = new LogParser(stream).parse();
          builder.consume(records);
        } catch (Exception e) {
          log.log(Level.SEVERE, "Ошибка при обработке файла " + filename, e);
          throw e;
        }
      }

      List<Record> results = builder.flush();
      results.forEach(System.out::println);
      new ReportExporter(config).export(results);
    } catch (Exception e) {
      fileSource.destroy();
      throw e;
    }
  }

  private void validateConfiguration() {
    LocalDateTime dateFrom = config.dateFrom;
    LocalDateTime dateTo = config.dateTo;
    Validate.notNull(dateFrom, "Параметр дата начала (dateFrom) не установлен");
    Validate.notNull(dateTo, "Параметр дата начала (dateTo) не установлен");
    Validate.isTrue(dateTo.isAfter(dateFrom), "Дата начала должна быть до даты конца");
  }

  private List<String> createFileList() {
    String from = FORMATTER.format(config.dateFrom) + ".csv";
    String to = FORMATTER.format(config.dateTo) + ".csv";

    List<String> fileList = fileSource.getFileList();
    Validate.isTrue(!fileList.isEmpty(), "Нет файлов в выбранной папке");
    return fileList.stream()
        .filter(name -> name.compareToIgnoreCase(from) >= 0 && name.compareToIgnoreCase(to) <= 0)
        .collect(Collectors.toList());
    // 2018.09.22_22_00.csv
  }
}
