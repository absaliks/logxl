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

package absaliks.logxl;

import absaliks.logxl.log.LogParser;
import absaliks.logxl.log.Record;
import java.io.InputStream;
import java.util.List;

public class Main {

  public static void main(String[] args) throws Exception {
    InputStream stream = Main.class.getClassLoader().getResourceAsStream("log.csv");
    if (stream != null) {
      long a = System.currentTimeMillis();
      List<Record> list = new LogParser(stream).parse();
      System.out.println("Records count: " + list.size());
      System.out.println("Elapsed time:  " + (System.currentTimeMillis() - a));
    } else {
      System.out.println("Stream is null");
    }
  }
}
