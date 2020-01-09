import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.openjdk.jmh.runner.RunnerException;

import enums.MetricGroup;
import model.HealthScoreContext;
import util.ChainUtil;
import util.DateTimeUtil;

public class HealthScoreCalculator {
  public static void main(String[] args) throws RunnerException, IOException {

    // TODO benchmark
    // Options opt =
    // new OptionsBuilder().include(HealthScoreCalculator.class.getSimpleName()).forks(1).build();
    //
    // new Runner(opt).run();

    // parse input
    final LocalDateTime dateTimeStart = DateTimeUtil.parseDateTime(args[0]);
    final LocalDateTime dateTimeEnd = DateTimeUtil.parseDateTime(args[1]);

    // validate input
    if (dateTimeStart == null || dateTimeEnd == null) {
      System.out.println("Please input valid arguments");
    }
    //
    // // build list of dateTime string for valid urls
    final List<String> urls =
        DateTimeUtil.buildDateTimeStringsFromInterval(dateTimeStart, dateTimeEnd);

    // download data parallely
    // urls.parallelStream().forEach(FileUtil::downloadAsJsonFile);

    //build context by metric
    HealthScoreContext context = HealthScoreContext.builder()
        .metricGroup(MetricGroup.ALL_METRIC).dateTimeStart(dateTimeStart).dateTimeEnd(dateTimeEnd)
        .build();

    // process data
    ChainUtil.executeChain(context);

    // delete files
  }


}
