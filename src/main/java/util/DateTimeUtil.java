package util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import constant.Constant;

public class DateTimeUtil {

  /**
   * parse date time string in format of ISO 8601
   *
   * @param dateTimeString String
   * @return LocalDateTime
   */
  public static LocalDateTime parseDateTime(String dateTimeString) throws DateTimeParseException {
    final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constant.ISO_DATE_TIME_FORMAT)
        .withZone(ZoneId.of(Constant.ZONE_UTC));

    return LocalDateTime.parse(dateTimeString, formatter);
  }

  /**
   * build list of dateTimeString based on the interval, follow this format: "yyyy-MM-dd-hh"
   *
   * @param dateTimeStart
   * @param dateTimeEnd
   * @return List<String>
   */
  public static List<String> buildDateTimeStringsFromInterval(LocalDateTime dateTimeStart,
      LocalDateTime dateTimeEnd) {
    if (dateTimeEnd.isBefore(dateTimeStart)) {
      return new ArrayList<>();
    }

    final List<String> dateTimeStrings = new ArrayList<>();
    LocalDateTime currentDateTime = LocalDateTime.from(dateTimeStart);

    // loop until dateTimeEnd
    while (!dateTimeEnd.isBefore(currentDateTime)) {
      // build URL
      final String url = new StringBuilder().append(currentDateTime.toLocalDate().toString())
          .append(Constant.HYPHENS).append(currentDateTime.getHour()).toString();

      // put to result list
      dateTimeStrings.add(url);

      // increase 1 hour
      currentDateTime = currentDateTime.plusHours(1);
    }

    return dateTimeStrings;
  }

  /**
   * check if time period is valid
   * 
   * @param time
   * @return
   */
  public static boolean isValidTime(Long time) {
    return time != null && time >= 0;
  }

}
