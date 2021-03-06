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
import absaliks.logxl.config.ConfigSerializer;
import absaliks.logxl.filesource.FtpFileSource;
import absaliks.logxl.filesource.LocalFileSource;
import absaliks.logxl.filesource.LogFileSource;
import absaliks.logxl.report.ReportService;

class Factory implements AbstractFactory {
  private final ConfigSerializer configSerializer;
  private final Config config;

  Factory() {
    configSerializer = new ConfigSerializer();
    config = configSerializer.load();
  }

  @Override
  public ConfigSerializer getConfigSerializer() {
    return configSerializer;
  }

  @Override
  public Config getConfig() {
    return config;
  }

  @Override
  public LogFileSource createLogFileSource() {
    switch (config.logsSource) {
      case FTP:
        return new FtpFileSource(config);
      case LOCAL_DIR:
        return new LocalFileSource(config.localDirectory);
    }
    throw new UnsupportedOperationException();
  }

  @Override
  public ReportService createReportService() {
    return new ReportService(this);
  }
}
