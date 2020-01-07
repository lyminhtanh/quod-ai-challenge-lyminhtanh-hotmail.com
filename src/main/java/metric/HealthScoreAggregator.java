package metric;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.chain.Context;
import org.apache.commons.chain.Filter;

import model.HealthScore;
import model.HealthScoreContext;
import model.Repo;

/**
 * Average number of commits (push) per day (to any branch) healthRatio = total(PushEvent of project
 * A)/total(PushEvent)
 */
public class HealthScoreAggregator implements Filter {

  @Override
  public boolean execute(Context context) throws Exception {
    return false;
  }

  @Override
  public boolean postprocess(Context context, Exception exception) {
    List<HealthScore> healthScores = ((HealthScoreContext) context).getHealthScores();

    Map<Repo, List<HealthScore>> groupedByRepo =
        healthScores.stream().collect(Collectors.groupingBy(HealthScore::getRepo));

    // aggregate heathScore by repo
    healthScores =
        groupedByRepo.values().stream().map(this::aggregateHealScore).collect(Collectors.toList());

    // update to context
    ((HealthScoreContext) context).setHealthScores(healthScores);

    return false;
  }


  /**
   * TODO using BigDecimal for more exact
   * https://stackoverflow.com/questions/5384601/java-doubles-are-not-good-at-math/5385202#5385202
   * return first object with final aggregate score
   *
   * @param healthScores
   * @return
   */
  private HealthScore aggregateHealScore(List<HealthScore> healthScores) {
    double finalHealthScore =
        healthScores.stream().mapToDouble(HealthScore::getScore).reduce(1.0, (a, b) -> a * b);

    healthScores.get(0).setScore(finalHealthScore);

    return healthScores.get(0);
  }


}
