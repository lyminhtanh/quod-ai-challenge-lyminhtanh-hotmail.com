import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import constant.Constant;
import enums.Strategy;
import lombok.extern.log4j.Log4j2;
import model.HealthScoreContext;
import util.ChainUtil;
import util.DateTimeUtil;
import util.FileUtil;

@Log4j2
public class HealthScoreCalculator {

  public static void main(String[] args) {

    try (Scanner input = new Scanner(System.in)) {
      HealthScoreContext context = buildContext(args);

      // prompt for download files
      showPromptForDownload(input, context);

      // Start calculating
      ChainUtil.executeChain(context);

      // prompt for delete files
      showPromptForDeleteFiles(input);
    } catch (Exception ex) {
      log.error("Failed to continue calculating.", ex);
    }
  }

  private static void showPromptForDeleteFiles(Scanner input) throws IOException {
    System.out.println(">>>> Do you want to delete all downloaded data? Input Y/N");

    if (Constant.ANSWER_YES.equalsIgnoreCase(input.next())) {
      FileUtil.deleteJsonFiles();

      log.info("Deleted all json files successfully");
    } else {
      log.info("All json files was kept. End calculating ...");
    }
  }

  private static void showPromptForDownload(Scanner input, HealthScoreContext context)
      throws IOException {
    System.out
        .println(">>>> Do you want to delete all downloaded data and download new one? Input Y/N");

    if (Constant.ANSWER_YES.equalsIgnoreCase(input.next())) {
      FileUtil.deleteJsonFiles();

      log.info("Deleted all json files successfully. Start downloading ...");

      // build list of dateTime string for valid urls
      final List<String> urls = DateTimeUtil
          .buildDateTimeStringsFromInterval(context.getDateTimeStart(), context.getDateTimeEnd());

      // download data parallely
      FileUtil.downloadAsJsonFile(urls);

    } else {
      log.info("All json files was kept. Start calculating ...");
    }
  }

  /**
   * build chain context by varargs
   * 
   * @param args
   * @return
   */
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
    final Strategy strategy =
        Optional.ofNullable(Strategy.valueOf(argList.get(2))).orElse(Strategy.ALL_METRIC);

    // validate input
    if (dateTimeStart == null || dateTimeEnd == null) {
      throw new IllegalStateException(
          "Please input valid arguments. E.g: 2019-08-01T00:00:00Z 2019-08-01T01:00:00Z. Pass at least 2 arguments dateTimeStart and dateTimeEnd.");
    }

    if (dateTimeStart.isAfter(dateTimeEnd)) {
      throw new IllegalStateException(
          "Please input valid arguments: dateTimeStart should be before dateTimeEnd.");

    }

    return HealthScoreContext.builder().strategy(strategy).dateTimeStart(dateTimeStart)
        .dateTimeEnd(dateTimeEnd).build();
  }


}
