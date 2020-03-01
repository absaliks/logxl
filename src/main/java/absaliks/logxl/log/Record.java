/*
 * LogXL is a program that reads log files from FTP and exports in Excel
 * Copyright (C) 2019  Shamil Absalikov
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

public class Record {
  public LocalDateTime datetime;
  public float[] values;
  public boolean isHeatingCableOn;
  public boolean isHeatingElementOn;

  @Override
  public String toString() {
    return "Record{" +
        "datetime=" + datetime +
        ", values=" + Arrays.toString(values) +
        ", isHeatingCableOn=" + isHeatingCableOn +
        ", isHeatingElementOn=" + isHeatingElementOn +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Record record = (Record) o;
    return isHeatingCableOn == record.isHeatingCableOn &&
        isHeatingElementOn == record.isHeatingElementOn &&
        datetime.equals(record.datetime) &&
        Arrays.equals(values, record.values);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(datetime, isHeatingCableOn, isHeatingElementOn);
    result = 31 * result + Arrays.hashCode(values);
    return result;
  }
}
