import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import enums.Strategy;
import lombok.extern.log4j.Log4j2;
import model.HealthScoreContext;
import util.ChainUtil;
import util.DateTimeUtil;
import util.FileUtil;

@Log4j2
public class HealthScoreCalculator {
  public static void main(String[] args) {

    // TODO benchmark
    // Options opt =
    // new OptionsBuilder().include(HealthScoreCalculator.class.getSimpleName()).forks(1).build();
    //
    // new Runner(opt).run();


    // process data
    try {
      HealthScoreContext context = buildContext(args);

      //
      // // build list of dateTime string for valid urls
      final List<String> urls =
          DateTimeUtil.buildDateTimeStringsFromInterval(context.getDateTimeStart(),
              context.getDateTimeEnd());

      // download data parallely
      urls.parallelStream().forEach(t -> {
        try {
          FileUtil.downloadAsJsonFile(t);
        } catch (MalformedURLException ex) {
          throw new RuntimeException(ex);
        } catch (IOException ex) {
          throw new RuntimeException(ex);
        }
      });

      ChainUtil.executeChain(context);

      // delete files
      // FileUtil.deleteJsonFiles();

    } catch (Exception ex) {
      log.error(String.format("Failed to execute Chain.", ex));
    }


  }

  private static HealthScoreContext buildContext(String[] args) {
    List<String> argList = new ArrayList<>(Arrays.asList(args));

    // pass at least 2 arguments dateTimeStart and dateTimeEnd
    if (argList.size() < 2) {
      throw new IllegalStateException(
          "Please input valid arguments. Pass at least 2 arguments dateTimeStart and dateTimeEnd. E.g: 2019-08-01T00:00:00Z 2019-08-01T01:00:00Z");
    } else if (argList.size() == 2) {
      // default strategy is ALL_METRIC
      argList.add(Strategy.ALL_METRIC.name());
    }

    // parse input
    final LocalDateTime dateTimeStart = DateTimeUtil.parseDateTime(argList.get(0));
    final LocalDateTime dateTimeEnd = DateTimeUtil.parseDateTime(argList.get(1));
    final Strategy metricGroup =
        Optional.ofNullable(Strategy.valueOf(argList.get(2))).orElse(Strategy.ALL_METRIC);

    // validate input
    if (dateTimeStart == null || dateTimeEnd == null) {
      throw new IllegalStateException(
          "Please input valid arguments. E.g: 2019-08-01T00:00:00Z 2019-08-01T01:00:00Z. Pass at least 2 arguments dateTimeStart and dateTimeEnd.");
    }

    return HealthScoreContext.builder().metricGroup(metricGroup).dateTimeStart(dateTimeStart)
        .dateTimeEnd(dateTimeEnd).build();
  }


}
