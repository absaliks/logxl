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

package absaliks.logxl.ui;

import static absaliks.logxl.log.LogsSource.FTP;
import static absaliks.logxl.log.LogsSource.LOCAL_DIR;

import absaliks.logxl.config.Config;
import absaliks.logxl.log.LogsSource;
import absaliks.logxl.report.ReportService;
import absaliks.logxl.report.ReportType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javax.inject.Inject;

public class DashboardPresenter {

  private static final Logger log = Logger.getLogger(DashboardPresenter.class.getName());
  private static final TimeFormatter TIME_FORMATTER = new TimeFormatter();
  private ExecutorService executor = Executors.newSingleThreadExecutor();

  @FXML
  private TextField userName;
  @FXML
  private TextField userPhone;

  @FXML
  private DatePicker dateFrom;
  @FXML
  private DatePicker dateTo;
  @FXML
  private ComboBox<String> timeFrom;
  @FXML
  private ComboBox<String> timeTo;
  @FXML
  private ChoiceBox<ReportType> reportType;
  @FXML
  private ChoiceBox<LogsSource> logsSource;
  @FXML
  private TextField directory;
  @FXML
  private TextField ftpServer;
  @FXML
  private Spinner<Integer> ftpPort;
  @FXML
  private TextField ftpLogin;
  @FXML
  private TextField ftpPassword;
  @FXML
  private CheckBox savePassword;
  @FXML
  private ProgressBar progressBar;

  @Inject
  private Config config;

  @Inject
  private ReportService reportService;

  private Future<?> future;

  public void generateReport() {
    if (future != null) {
      showAlert(AlertType.INFORMATION, null, "Дождитесь окончания генерации текущего отчета");
    }
    future = executor.submit(() -> {
      try {
        reportService.createReport();
      } catch (Exception e) {
        log.log(Level.SEVERE, "Unable to create a report with settings " + config, e);
        Platform.runLater(() ->
            showAlert(AlertType.ERROR, "Не удалось создать отчет", e.getMessage()));
      } finally {
        future = null;
      }
    });
  }

  @FXML
  private void initialize() {
    initReportTypeControl();
    initLogsSourceControl();
    initTimeControls();
    initFtpPortControl();
    initFtpServerControl();
    initFtpLoginControl();
    initFtpPasswordControl();
    initDirectoryControl();
    initUserNameControl();
    initPhoneControl();
    initSavePasswordCheckbox();
    progressBar.progressProperty().bind(reportService.progress());
    refreshFTPControlsAvailability();
  }

  void shutdown() {
    executor.shutdown();
  }

  private void initSavePasswordCheckbox() {
    savePassword.setSelected(config.savePassword);
    savePassword.setOnAction(e -> config.savePassword = savePassword.isSelected());
  }

  private void initReportTypeControl() {
    initEnumChoiceBox(reportType, ReportType.class, config.reportType);
    reportType.setOnAction((e) -> config.reportType = reportType.getValue());
  }

  private void initLogsSourceControl() {
    config.logsSource = FTP;
    initEnumChoiceBox(logsSource, LogsSource.class, config.logsSource);
    logsSource.setOnAction((e) -> {
      config.logsSource = logsSource.getValue();
      refreshFTPControlsAvailability();
      refreshDirectoryControlValue();
    });
  }

  private void refreshFTPControlsAvailability() {
    boolean isNotFtp = config.logsSource != FTP;
    ftpServer.setDisable(isNotFtp);
    ftpPort.setDisable(isNotFtp);
    ftpLogin.setDisable(isNotFtp);
    ftpPassword.setDisable(isNotFtp);
  }

  private <T extends Enum<T>> void initEnumChoiceBox(ChoiceBox<T> choiceBox, Class<T> enumClass,
      T configValue) {
    T[] elements = enumClass.getEnumConstants();
    choiceBox.getItems().addAll(elements);
    choiceBox.setValue(configValue != null ? configValue : elements[0]);
  }

  private void initTimeControls() {
    final List<String> timeElements = new ArrayList<>(25);
    IntStream.range(0, 25)
        .mapToObj(i -> String.format("%02d:00", i))
        .forEach(timeElements::add);
    timeElements.add("23:59:59");

    timeFrom.getItems().addAll(timeElements);
    timeFrom.setValue(timeElements.get(0));
    setDateValue(this.dateFrom, this.timeFrom, config.dateFrom);

    EventHandler<ActionEvent> dateFromUpdater =
        e -> config.dateFrom = TIME_FORMATTER.buildDate(dateFrom.getValue(), timeFrom.getValue());
    this.dateFrom.setOnAction(dateFromUpdater);
    this.timeFrom.setOnAction(dateFromUpdater);

    timeTo.getItems().addAll(timeElements);
    timeTo.setValue(timeElements.get(timeElements.size() - 1));
    setDateValue(this.dateTo, this.timeTo, config.dateTo);

    EventHandler<ActionEvent> dateToUpdater =
        e -> config.dateTo = TIME_FORMATTER.buildDate(dateTo.getValue(), timeTo.getValue());
    this.dateTo.setOnAction(dateToUpdater);
    this.timeTo.setOnAction(dateToUpdater);
  }

  private void setDateValue(DatePicker datePicker,
      ComboBox<String> timeFrom, LocalDateTime date) {
    datePicker.setValue(date.toLocalDate());
    timeFrom.setValue(TIME_FORMATTER.format(date.toLocalTime()));
  }

  private void initFtpPortControl() {
    int initialValue = config.ftpPort == 0 ? config.ftpPort : 21;
    final IntegerSpinnerValueFactory factory
        = new IntegerSpinnerValueFactory(1, 65535, initialValue);
    ftpPort.setValueFactory(factory);
    ftpPort.valueProperty().addListener((e, o, newValue) -> config.ftpPort = newValue);

    final TextFormatter<Integer> formatter =
        new TextFormatter<>(factory.getConverter(), factory.getValue());
    ftpPort.getEditor().setTextFormatter(formatter);
    factory.valueProperty().bindBidirectional(formatter.valueProperty());
  }

  private void initFtpServerControl() {
    ftpServer.setText(config.ftpServer);
    ftpServer.textProperty().addListener((e, oldValue, newValue) -> config.ftpServer = newValue);
  }

  private void initFtpLoginControl() {
    ftpLogin.setText(config.ftpLogin);
    ftpLogin.textProperty().addListener(e -> config.ftpLogin = ftpLogin.getText());
  }

  private void initFtpPasswordControl() {
    ftpPassword.setText(config.ftpPassword);
    ftpPassword.textProperty().addListener(e -> config.ftpPassword = ftpPassword.getText());
  }

  private void initDirectoryControl() {
    refreshDirectoryControlValue();
    directory.textProperty().addListener(e -> {
      if (config.logsSource == FTP) {
        config.ftpDirectory = directory.getText();
      } else {
        config.localDirectory = directory.getText();
      }
    });
  }

  private void refreshDirectoryControlValue() {
    directory.setText(config.logsSource != LOCAL_DIR ? config.ftpDirectory : config.localDirectory);
  }

  private void initUserNameControl() {
    userName.setText(config.userName);
    userName.textProperty().addListener(e -> config.userName = userName.getText());
  }

  private void initPhoneControl() {
    userPhone.setText(config.userPhone);
    userPhone.textProperty().addListener(e -> config.userPhone = userPhone.getText());
  }

  private static void showAlert(AlertType alertType, String title, String text) {
    Alert alert = new Alert(alertType, text);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.showAndWait();
  }
}
