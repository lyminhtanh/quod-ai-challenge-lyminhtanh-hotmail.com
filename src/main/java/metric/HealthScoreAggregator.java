package metric;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.chain.Context;
import org.apache.commons.chain.Filter;

import model.HealthScore;
import model.HealthScoreContext;
import util.NormalizeUtil;

/**
 * Average number of commits (push) per day (to any branch) healthRatio = total(PushEvent of project
 * A)/total(PushEvent)
 */
public class HealthScoreAggregator implements Filter {

  @Override
  public boolean execute(Context context) throws Exception {
    List<HealthScore> healthScores = ((HealthScoreContext) context).getHealthScores();

    // normalize scores
    NormalizeUtil.normalize(healthScores, ((HealthScoreContext) context).getMetricGroup());

    // calculate final score
    aggregateHealScore(healthScores);

    // sort descending by score
    healthScores.sort(Comparator.comparing(HealthScore::getScore,
        Comparator.nullsLast(Comparator.reverseOrder())));

    // update repo names
    ConcurrentMap<Long, String> repoNameMap = ((HealthScoreContext) context).getRepoNames();
    healthScores.parallelStream().forEach(healthScore -> {
      healthScore.setRepoName(repoNameMap.get(healthScore.getRepoId()));
    });
    // update to context
    ((HealthScoreContext) context).setHealthScores(healthScores);

    return false;
  }

  @Override
  public boolean postprocess(Context context, Exception exception) {

    return false;
  }

  /**
   * return first object with final aggregate score
   *
   * @param healthScores
   * @return
   */
  private void aggregateHealScore(List<HealthScore> healthScores) {
    healthScores.forEach(healthScore -> {
      Double aggregateScore = healthScore.getSingleMetricScores().values().stream()
          .mapToDouble(Double::doubleValue).reduce(1.0, (a, b) -> a * b);

      healthScore.setScore(aggregateScore);
    });
  }

}
