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

package absaliks.logxl.ftp;

import absaliks.logxl.config.Config;
import absaliks.logxl.report.LogFileSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

@Log
public class FtpFileSource implements LogFileSource {

  private static final String CACHE_FOLDER = "cache";

  private final Config config;
  private FTPClient ftpClient = new FTPClient();

  public FtpFileSource(Config config) {
    this.config = config;
  }

  @Override
  public void initialize() {
    tryConnect();
    tryLogin();
  }

  @Override
  public void destroy() {
    tryDisconnect();
  }

  @Override
  @SneakyThrows(IOException.class)
  public List<String> getFileList() {
    return Arrays.stream(ftpClient.listFiles(config.ftpDirectory))
        .filter(FTPFile::isFile)
        .map(FTPFile::getName)
        .collect(Collectors.toList());
  }

  @Override
  public File getFile(String filename) {
    File cachedFile = new File("cache/" + filename);
    if (cachedFile.exists()) {
      cachedFile.delete();
    } else {
      cachedFile.getParentFile().mkdirs();
    }
    retrieveFile(filename, cachedFile);
    return cachedFile;
  }

  private void retrieveFile(String remoteFilename, File localFile) {
    try (FileOutputStream stream = new FileOutputStream(localFile)) {
      this.ftpClient.retrieveFile(config.ftpDirectory + "/" + remoteFilename, stream);
    } catch (IOException e) {
      log.log(Level.WARNING, "Не удалось скачать файл " + remoteFilename);
      e.printStackTrace();
    }
  }

  private void tryLogin() {
    try {
      if (ftpClient.login(config.ftpLogin, config.ftpPassword)) {
        log.info("Авторизация - OK");
      } else {
        throw new LoginException("Сервер отклонил попытку авторизации - проверьте логин/пароль");
      }
    } catch (IOException e) {
      throw new LoginException("Не удалось авторизоваться", e);
    }
  }

  private void tryConnect() {
    try {
      ftpClient.connect(config.ftpServer, config.ftpPort);
      log.info("Подключение к FTP серверу - ОК");
    } catch (IOException e) {
      tryDisconnect();
      throw new ConnectionException(
          "Не удалось подключиться к серверу, проверьте параметры соединения", e);
    }
  }

  private void tryDisconnect() {
    if (ftpClient.isConnected()) {
      try {
        ftpClient.disconnect();
        log.info("Отключение от FTP сервера - ОК");
      } catch (IOException e) {
        throw new ConnectionException("Не удалось закрыть соединение с FTP сервером", e);
      }
    }
  }
}
