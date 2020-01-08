import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.chain.Command;

import enums.Metric;
import enums.MetricGroup;
import metric.MetricCatalog;
import model.HealthScoreContext;
import util.ChainUtil;
import util.DateTimeUtil;
import util.FileUtil;

public class HealthScoreCalculator {
  public static void main(String[] args) {
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
//    urls.parallelStream().forEach(FileUtil::downloadAsJsonFile);

    //build context by metric
    HealthScoreContext context = HealthScoreContext.builder()
    		.metricGroup(MetricGroup.all_metric)
    		.dateTimeStart(dateTimeStart)
    		.dateTimeEnd(dateTimeEnd)
    		.build();
    
    // process data
    ChainUtil.executeChain(context);

    // delete files
  }


}
