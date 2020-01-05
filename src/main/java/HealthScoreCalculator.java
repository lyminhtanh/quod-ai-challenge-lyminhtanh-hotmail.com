import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import enums.GitHubEventType;
import metric.AverageCommitHeathMetric;
import model.GitHubEvent;
import model.Repo;
import util.DateTimeUtil;
import util.FileUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HealthScoreCalculator {
    public static void main(String[] args) {
        // parse input
        final LocalDateTime dateTimeStart = DateTimeUtil.parseDateTime(args[0]);
        final LocalDateTime dateTimeEnd = DateTimeUtil.parseDateTime(args[1]);

        // validate input
        if (dateTimeStart == null || dateTimeEnd == null) {
            System.out.println("Please input valid arguments");
        }

        // build list of dateTime string for valid urls
        final List<String> urls = DateTimeUtil.buildDateTimeStringsFromInterval(dateTimeStart, dateTimeEnd);

        // download data parallely
        urls.parallelStream().forEach(FileUtil::downloadAsJsonFile);

        // process data
        new AverageCommitHeathMetric().calculate();

        // delete files
    }
}
