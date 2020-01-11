package util;

import org.apache.commons.chain.Command;

import lombok.extern.log4j.Log4j2;
import metric.MetricCatalog;
import model.HealthScoreContext;

@Log4j2
public class ChainUtil {

  /**
   * execute Chain by context
   *
   * @param context
   * @throws Exception
   */
  public static void executeChain(HealthScoreContext context) throws Exception {
    Command allMetricChain = new MetricCatalog().getCommand(context.getStrategy().name());

    if (allMetricChain == null) {
      throw new RuntimeException(
          String.format("No Chain found for the strategy %s", context.getStrategy()));
    }

    log.info("--- Start chain with the strategy {}", context.getStrategy());

    allMetricChain.execute(context);
  }

}
