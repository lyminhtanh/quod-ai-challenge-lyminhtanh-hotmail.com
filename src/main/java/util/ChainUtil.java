package util;

import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import org.apache.commons.chain.Command;

import enums.Metric;
import lombok.extern.log4j.Log4j2;
import metric.MetricCatalog;
import model.HealthScore;
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
    Command allMetricChain = new MetricCatalog().getCommand(context.getMetricGroup().name());
    allMetricChain.execute(context);
  }

  /**
   * merge results in to context's healthScores
   *
   * @param ctxHealthScores
   * @param currentMetricHealthScores
   */
  public static void mergeHealthScores(List<HealthScore> ctxHealthScores,
      List<HealthScore> currentMetricHealthScores, Metric metric) {


    final Set<Long> ctxRepoIds =
        ctxHealthScores.stream().map(HealthScore::getRepoId).collect(Collectors.toSet());

    List<HealthScore> additionalHealthScores = new Vector<>();
    currentMetricHealthScores.parallelStream().forEach(healthScore -> {
      Long repoId = healthScore.getRepoId();

      // multiple score if repo exists
      if (ctxRepoIds.contains(repoId)) {
        ctxHealthScores.parallelStream().filter(ctxHs -> ctxHs.getRepoId().equals(repoId)).findAny()
            // TODO update to map single
            .ifPresent(ctxHs -> {
              ctxHs.getSingleMetricScores().put(metric,
                  healthScore.getSingleMetricScores().get(metric));
              // additionalUpdatedFiedls.forEach(biCon -> biCon.accept(ctxHs, ););
            });

      } else { // add new repo
        additionalHealthScores.add(healthScore);
      }
    });

    // add all new repo
    ctxHealthScores.addAll(additionalHealthScores);
  }
}
