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
import absaliks.logxl.log.Record;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.commons.lang3.Validate;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

class ReportExporter {

  private static final Logger log = Logger.getLogger(ReportExporter.class.getName());

  private static final String TEMPLATE_FILE = "template.xlsx";
  private static final File OUTPUT_FILE = new File("report.xlsx");
  private static final int ROW_OFFSET = 8;
  private static final Byte[] VALUES_DECIMAL_PLACES = {
      2, 2, 2, 2, 2, 2, 1, 2, 2, 3, 3, 3, 2, 2, 3, 3, 3, 2, 1, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2
  };

  private final Config config;
  private SXSSFWorkbook workbook;
  private CellStyle borderedCellStyle;
  private CellStyle dateStyle;
  private Map<Byte, CellStyle> floatRoundStyles;

  ReportExporter(Config config) {
    this.config = config;
  }

  void export(List<Record> records) throws IOException {
    Validate.notEmpty(records, "Результат пустой, нечего выгружать");
    deleteReportFile();

    log.info("Выгружаю отчет в файл " + OUTPUT_FILE.getAbsolutePath());

    XSSFWorkbook template = new XSSFWorkbook(getTemplateFileStream());
    fillMetaData(template.getSheetAt(0));
    this.workbook = new SXSSFWorkbook(template, 100);
    Sheet sheet = this.workbook.getSheetAt(0);
    floatRoundStyles = createFloatRoundStyles();
    dateStyle = createDateStyle();
    borderedCellStyle = createBorderedCellStyle();
    for (int i = 0; i < records.size(); i++) {
      Row row = sheet.createRow(i + ROW_OFFSET);
      Record rec = records.get(i);
      addDateCell(rec, row);
      addMeasurementsCells(rec.values, row);
      addBooleanCell(rec.isHeatingCableOn, row, 32);
      addBooleanCell(rec.isHeatingElementOn, row, 33);
    }

    FileOutputStream out = new FileOutputStream(OUTPUT_FILE);
    this.workbook.write(out);
    out.close();

    this.workbook.dispose();
  }

  private InputStream getTemplateFileStream() {
    InputStream stream = ReportExporter.class.getClassLoader().getResourceAsStream(TEMPLATE_FILE);
    if (stream == null) {
      log.severe("Template file not found " + TEMPLATE_FILE);
      throw new RuntimeException("Шаблон для выгрузки не найден: " + TEMPLATE_FILE);
    }
    return stream;
  }

  private void fillMetaData(Sheet sheet) {
    getMetadataCell(sheet, 0).setCellValue(config.userName);
    getMetadataCell(sheet, 1).setCellValue(config.userPhone);
  }

  private Cell getMetadataCell(Sheet sheet, int y) {
    Row row = sheet.getRow(y);
    row = row != null ? row : sheet.createRow(y);
    Cell cell = row.getCell(3);
    return cell != null ? cell : row.createCell(3);
  }

  private Map<Byte, CellStyle> createFloatRoundStyles() {
    final Map<Byte, CellStyle> styles = new HashMap<>(3);
    for (byte i = 1; i <= 3; i++) {
      CellStyle dataStyle = createBorderedCellStyle();
      String format;
      switch (i) {
        case 1:
          format = "0.#";
          break;
        case 2:
          format = "0.##";
          break;
        case 3:
          format = "0.###";
          break;
        default:
          throw new IllegalStateException();
      }
      dataStyle.setDataFormat(workbook.createDataFormat().getFormat(format));
      styles.put(i, dataStyle);
    }
    return styles;
  }

  private CellStyle createBorderedCellStyle() {
    CellStyle style = workbook.createCellStyle();
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    return style;
  }

  private CellStyle createDateStyle() {
    CellStyle style = workbook.createCellStyle();
    String excelFormatPattern = DateFormatConverter.convert(Locale.US, getDateFormat());
    DataFormat poiFormat = workbook.createDataFormat();
    style.setDataFormat(poiFormat.getFormat(excelFormatPattern));
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    return style;
  }

  private String getDateFormat() {
    switch (config.reportType) {
      case DAILY: return "dd.MM.yyyy";
      case STRAIGHT: return "dd.MM.yyyy HH:mm:ss";
      default: return "dd.MM.yyyy HH:mm";
    }
  }

  static void deleteReportFile() throws IOException {
    if (OUTPUT_FILE.exists()) {
      if (!OUTPUT_FILE.delete()) {
        throw new IOException("Не могу удалить файл " + OUTPUT_FILE.getAbsolutePath());
      }
    }
  }

  private void addDateCell(Record rec, Row row) {
    Cell cell = row.createCell(0);
    cell.setCellValue(java.sql.Timestamp.valueOf(rec.datetime));
    cell.setCellStyle(dateStyle);
  }

  private void addMeasurementsCells(float[] values, Row row) {
    for (int i = 0; i < values.length; i++) {
      Cell cell = row.createCell(i + 1);
      cell.setCellValue(values[i]);
      cell.setCellStyle(floatRoundStyles.get(VALUES_DECIMAL_PLACES[i]));
    }
  }

  private void addBooleanCell(boolean value, Row row, int columnNumber) {
    final Cell cell = row.createCell(columnNumber);
    cell.setCellValue(value ? "ВКЛ" : "ВЫКЛ");
    cell.setCellStyle(borderedCellStyle);
  }
}
