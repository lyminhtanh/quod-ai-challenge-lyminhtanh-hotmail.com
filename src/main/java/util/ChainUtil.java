package util;

import org.apache.commons.chain.Command;

import metric.MetricCatalog;
import model.HealthScoreContext;

public class ChainUtil {

  /**
   * execute Chain by context
   *
   * @param context
   * @throws Exception
   */
  public static void executeChain(HealthScoreContext context) throws Exception {
    Command allMetricChain = new MetricCatalog().getCommand(context.getMetricGroup().name());
    allMetricChain.execute(context);
  }

}
