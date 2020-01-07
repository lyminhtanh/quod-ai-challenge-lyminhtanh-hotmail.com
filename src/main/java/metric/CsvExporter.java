package metric;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.chain.Context;
import org.apache.commons.chain.Filter;

import constant.Constant;
import model.HealthScore;
import model.HealthScoreContext;
import util.FileUtil;

/**
 * Average number of commits (push) per day (to any branch) healthRatio = total(PushEvent of project
 * A)/total(PushEvent)
 */
public class CsvExporter implements Filter {

  @Override
  public boolean execute(Context context) throws Exception {
    return false;
  }

  @Override
  public boolean postprocess(Context context, Exception exception) {
    HealthScoreContext healthScoreContext = (HealthScoreContext) context;
    List<String[]> csvRows = healthScoreContext.getHealthScores().stream()
        .map(HealthScore::toCsvRow).collect(Collectors.toList());

    try {
      FileUtil.createCSVFile(csvRows);
      System.out.println(String.format("Exported result to: %s", Constant.OUTPUT_FILE_NAME));
    } catch (IOException ex) {
      System.out.println(String.format("Failed to Export result. %s", ex));
    }
    return false;
  }



}
