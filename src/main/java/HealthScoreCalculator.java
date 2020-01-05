import util.DateTimeUtil;
import util.FileUtil;

import java.time.LocalDateTime;
import java.util.List;

public class HealthScoreCalculator {
    public static void main(String[] args){
        // parse input
        LocalDateTime dateTimeStart = DateTimeUtil.parseDateTime(args[0]);
        LocalDateTime dateTimeEnd = DateTimeUtil.parseDateTime(args[1]);

        // validate input
        if(dateTimeStart == null  || dateTimeEnd == null){
            System.out.println("Please input valid arguments");
        }

        // build list of dateTime string for valid urls
        List<String> urls =  DateTimeUtil.buildDateTimeStringsFromInterval(dateTimeStart, dateTimeEnd);

        // download data parallely
        urls.parallelStream().forEach(FileUtil::downloadAsJsonFile);


        // process data

        // delete files
    }
}
