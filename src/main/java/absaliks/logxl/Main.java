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

import absaliks.logxl.config.Config;
import absaliks.logxl.report.ReportService;
import absaliks.logxl.ui.JFXLauncher;
import com.airhacks.afterburner.injection.Injector;
import javafx.application.Application;

public class Main {

  static {
    System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tT %4$s: %5$s %6$s%n");
  }

  private static final Factory factory = new Factory();

  public static void main(String[] args) {
    Injector.setModelOrService(Config.class, factory.getConfig());
    Injector.setModelOrService(ReportService.class, factory.createReportService());

    Application.launch(JFXLauncher.class);
    factory.getConfigSerializer().save(factory.getConfig());
  }
}
