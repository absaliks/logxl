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
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Log
@RequiredArgsConstructor
public class ReportExporter {

  private static final int ROW_OFFSET = 8;
  private static final File FILE = new File("report.xlsx");

  private final Config config;
  private SXSSFWorkbook workbook;
  private CellStyle dataStyle;
  private CellStyle dateStyle;
  private Sheet sheet;

  public void export(List<Record> records) throws IOException {
    Validate.notEmpty(records, "Результат пустой, нечего выгружать");
    deleteReportFile();

    log.info("Выгружаю отчет в файл " + FILE.getAbsolutePath());

    InputStream stream = ReportExporter.class.getClassLoader().getResourceAsStream("template.xlsx");
    XSSFWorkbook template = new XSSFWorkbook(stream);
    fillMetaData(template);
    this.workbook = new SXSSFWorkbook(template, 100);
    sheet = this.workbook.getSheetAt(0);
    createDataStyle();
    createDateStyle();
    for (int i = 0; i < records.size(); i++) {
      Row row = sheet.createRow(i + ROW_OFFSET);
      Record rec = records.get(i);
      addDateCell(rec, row);
      addMeasurementsCells(rec.values, row);
    }

    FileOutputStream out = new FileOutputStream(FILE);
    this.workbook.write(out);
    out.close();

    this.workbook.dispose();
  }

  private void fillMetaData(XSSFWorkbook template) {
    XSSFSheet s = template.getSheetAt(0);
    // TODO: add null-guards
    setCellValue(s.getRow(0).getCell(3), config.userName);
    s.getRow(1).getCell(3).setCellValue(config.userPhone);
  }

  @SneakyThrows(UnsupportedEncodingException.class)
  private void setCellValue(Cell cell, String string) {
    // Convert from Unicode to UTF-8
    byte[] utf8 = string.getBytes("UTF-8");

    // Convert from UTF-8 to Unicode
    cell.setCellValue(new String(utf8, "UTF-8"));
  }

  private void createDataStyle() {
    dataStyle = workbook.createCellStyle();
    dataStyle.setBorderBottom(BorderStyle.THIN);
    dataStyle.setBorderLeft(BorderStyle.THIN);
    dataStyle.setBorderRight(BorderStyle.THIN);
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

  private void addMeasurementsCells(float[] values, Row row) {
    for (int i = 0; i < values.length; i++) {
      Cell cell = row.createCell(i + 1);
      cell.setCellValue(values[i]);
      cell.setCellStyle(dataStyle);
    }
  }

  private void addDateCell(Record rec, Row row) {
    Cell cell = row.createCell(0);
    cell.setCellValue(java.sql.Timestamp.valueOf(rec.datetime));
    cell.setCellStyle(dateStyle);
  }


}
