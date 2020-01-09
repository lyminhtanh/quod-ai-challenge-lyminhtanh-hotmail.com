import java.time.LocalDateTime;
import java.util.List;

import enums.MetricGroup;
import model.HealthScoreContext;
import util.ChainUtil;
import util.DateTimeUtil;

public class HealthScoreCalculator {
  public static void main(String[] args) {

    // TODO benchmark
    // Options opt =
    // new OptionsBuilder().include(HealthScoreCalculator.class.getSimpleName()).forks(1).build();
    //
    // new Runner(opt).run();


    // process data
    try {
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
      // urls.parallelStream().forEach(t -> {
      // try {
      // FileUtil.downloadAsJsonFile(t);
      // } catch (MalformedURLException e) {
      // throw new RuntimeException("Download files failed because of URL. %s", e);
      // } catch (IOException e) {
      // throw new RuntimeException("Download files successfully but failed to save. %s", e);
      // }
      // });

      // build context by metric
      HealthScoreContext context = HealthScoreContext.builder().metricGroup(MetricGroup.ALL_METRIC)
          .dateTimeStart(dateTimeStart).dateTimeEnd(dateTimeEnd).build();

      ChainUtil.executeChain(context);
    } catch (Exception ex) {
      System.out.println(String.format("Failed to execute Chain. %s", ex));
    }

    // delete files
  }


}
