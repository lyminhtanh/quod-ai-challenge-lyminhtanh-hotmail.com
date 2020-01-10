package metric;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import enums.GitHubEventType;
import enums.Metric;
import model.GitHubEvent;
import model.HealthScore;

/**
 * Average number of commits (push) per day (to any branch) healthRatio =
 * total(PushEvent of project A)/total(PushEvent)
 */

public class AverageCommitHeathMetric extends HealthMetric {

  public AverageCommitHeathMetric() throws IOException {
    super(Metric.average_commit, GitHubEventType.PUSH_EVENT);
  }

  @Override
  protected HealthScore calculateHealthScore(Entry<Long, List<GitHubEvent>> entry) {
    HealthScore healthScore =
        HealthScore.commonBuilder(this.context.getMetricGroup()).repoId(entry.getKey())
        .numOfCommit(entry.getValue().size()).build();

    healthScore.getSingleMetricScores().put(this.metric, (double) healthScore.getNumOfCommit());

    return healthScore;
  }




}
