/*
 * LogXL is a program that reads log files from FTP and exports in Excel
 * Copyright (C) 2020  Shamil Absalikov
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

package absaliks.logxl.log;

import static org.apache.commons.lang3.StringUtils.split;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class RecordTest {

  @Test
  void oldRecord() {
    final String line =
        "2018.10.29_18:00:00;-0,007629510946571827;39,55;0,2441443502902985;39,46;0,156404972076416;9,950000000000001;129,56;0,02232803218066692;9,43;0;0;0;0,01658451743423939;9,49;0;0;0;0,156404972076416;0,1411459594964981;0;236,83;5,68;2,46;59,15;11,5;-0,03051804378628731;9;0;0,1182574182748795;9,6;8,99;9,540000000000001;0;";
    String[] parts = split(line, ';');
    assertEquals(34, parts.length);
  }

  @Test
  void record_2020() {
    final String line =
        "2019.03.22_06:30:56;-50,02;-1,97;-49,61;-1,76;-49,72;-1,31;-6,49;0,032705586;-1,63;0;0;0;0,001278427;-2,03;0;0;0;-49,81;99,38;0;193,68;4,65;2,12;50,9;11,5;-50,05;-2,03;-1,65;-49,85;-1,93;-1,69;-1,6;0;0";
    String[] parts = split(line, ';');
    assertEquals(35, parts.length);
  }
}
