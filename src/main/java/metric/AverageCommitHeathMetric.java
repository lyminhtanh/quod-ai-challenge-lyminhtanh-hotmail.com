package metric;

import java.io.IOException;
import java.util.List;

import enums.Metric;
import enums.StatisticData;
import model.GitHubEvent;

/**
 * Average number of commits (push) per day (to any branch) count of PushEvents of project A
 */

public class AverageCommitHeathMetric extends HealthMetric {

  public AverageCommitHeathMetric() throws IOException {
    super(Metric.AVERAGE_COMMIT);
  }

  @Override
  protected double calculateHealthScore(List<GitHubEvent> repoEvents) {
    final long repoId = getRepoId(repoEvents);

    int numOfCommits = repoEvents.size();

    // update statistic data
    getRepoStatistics(repoId).put(StatisticData.NUM_OF_COMMITS, numOfCommits);

    return numOfCommits;
  }
}
