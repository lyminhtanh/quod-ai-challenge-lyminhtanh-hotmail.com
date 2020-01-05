package util;

import constant.Constant;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class DateTimeUtil {

    /**
     * parse date time string in format of ISO 8601
     *
     * @param dateTimeString String
     * @return LocalDateTime
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constant.ISO_DATE_TIME_FORMAT)
                .withZone(ZoneId.of(Constant.ZONE_UTC));

        LocalDateTime dateTime = null;
        try {
            dateTime = LocalDateTime.parse(dateTimeString, formatter);
        } catch (DateTimeParseException ex) {
            System.out.println(String.format("%s can not be parsed.", dateTimeString));
        }
        return dateTime;
    }

    /**
     * build list of dateTimeString based on the interval, follow this format:
     * "yyyy-MM-dd-hh"
     * @param dateTimeStart
     * @param dateTimeEnd
     * @return List<String>
     */
    public static List<String> buildDateTimeStringsFromInterval(LocalDateTime dateTimeStart, LocalDateTime dateTimeEnd) {
        if (dateTimeEnd.isBefore(dateTimeStart)) {
            return new ArrayList<>();
        }

        final List<String> dateTimeStrings = new ArrayList<>();
        LocalDateTime currentDateTime = LocalDateTime.from(dateTimeStart);

        // loop until dateTimeEnd
        while (!dateTimeEnd.isBefore(currentDateTime)) {
            // build URL
            final String url = new StringBuilder()
                    .append(currentDateTime.toLocalDate().toString())
                    .append(Constant.HYPHENS)
                    .append(currentDateTime.getHour())
                    .toString();

            // put to result list
            dateTimeStrings.add(url);

            // increase 1 hour
            currentDateTime = currentDateTime.plusHours(1);
        }

        return dateTimeStrings;
    }

}
