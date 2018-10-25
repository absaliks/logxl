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
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
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

@Log
@RequiredArgsConstructor
public class ReportExporter {

  private static final int ROW_OFFSET = 8;
  private static final File FILE = new File("report.xlsx");
  private static final Byte[] VALUES_DECIMAL_PLACES = {
      2, 2, 2, 2, 2, 2, 1, 2, 2, 3, 3, 3, 2, 2, 3, 3, 3, 2, 1, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2
  };

  private final Map<Byte, CellStyle> DATA_ROUND_STYLES = new HashMap<>(3);

  private final Config config;
  private SXSSFWorkbook workbook;
  private CellStyle heatingStyle;
  private CellStyle dateStyle;

  public void export(List<Record> records) throws IOException {
    Validate.notEmpty(records, "Результат пустой, нечего выгружать");
    deleteReportFile();

    log.info("Выгружаю отчет в файл " + FILE.getAbsolutePath());

    InputStream stream = ReportExporter.class.getClassLoader().getResourceAsStream("template.xlsx");
    XSSFWorkbook template = new XSSFWorkbook(stream);
    fillMetaData(template.getSheetAt(0));
    this.workbook = new SXSSFWorkbook(template, 100);
    Sheet sheet = this.workbook.getSheetAt(0);
    createDataStyles();
    createDateStyle();
    heatingStyle = createDataStyle();
    for (int i = 0; i < records.size(); i++) {
      Row row = sheet.createRow(i + ROW_OFFSET);
      Record rec = records.get(i);
      addDateCell(rec, row);
      addMeasurementsCells(rec.values, row);
      addHeatingCells(rec.isHeatingOn, row);
    }

    FileOutputStream out = new FileOutputStream(FILE);
    this.workbook.write(out);
    out.close();

    this.workbook.dispose();
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

  private void createDataStyles() {
    for (byte i = 1; i <= 3; i++) {
      CellStyle dataStyle = createDataStyle();
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
      DATA_ROUND_STYLES.put(i, dataStyle);
    }
  }

  private CellStyle createDataStyle() {
    CellStyle style = workbook.createCellStyle();
    style.setBorderBottom(BorderStyle.THIN);
    style.setBorderLeft(BorderStyle.THIN);
    style.setBorderRight(BorderStyle.THIN);
    return style;
  }

  private void createDateStyle() {
    dateStyle = workbook.createCellStyle();
    String format = config.reportType == ReportType.DAILY ? "dd.MM.yyyy" : "dd.MM.yyyy HH:mm";
    String excelFormatPattern = DateFormatConverter.convert(Locale.US, format);
    DataFormat poiFormat = workbook.createDataFormat();
    dateStyle.setDataFormat(poiFormat.getFormat(excelFormatPattern));
    dateStyle.setBorderBottom(BorderStyle.THIN);
    dateStyle.setBorderLeft(BorderStyle.THIN);
  }

  static void deleteReportFile() throws IOException {
    if (FILE.exists()) {
      if (!FILE.delete()) {
        throw new IOException("Не могу удалить файл " + FILE.getAbsolutePath());
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
      cell.setCellStyle(DATA_ROUND_STYLES.get(VALUES_DECIMAL_PLACES[i]));
    }
  }

  private void addHeatingCells(boolean isHeatingOn, Row row) {
    Cell cell = row.createCell(31);
    cell.setCellValue(isHeatingOn ? "ВКЛ" : "ВЫКЛ");
    cell.setCellStyle(heatingStyle);
  }
}
