package metric;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import enums.GitHubEventType;
import enums.Metric;
import lombok.extern.log4j.Log4j2;
import model.Actor;
import model.GitHubEvent;
import model.HealthScore;

/**
 * Ratio of commit per developers
 */
@Log4j2
public class AverageCommitPerDeveloperRatioHeathMetric extends HealthMetric {

  public AverageCommitPerDeveloperRatioHeathMetric() throws IOException {
    super(Metric.average_commit_per_developer_ratio, GitHubEventType.PUSH_EVENT);
  }

  @Override
  public List<HealthScore> calculate() throws IOException {
    log.debug("--start calculate");

    List<HealthScore> healthScores = events.entrySet().parallelStream()
        .map(this::buildHealthScore).collect(Collectors.toCollection(Vector::new));
    log.debug("--end calculate");
    return healthScores;
  }

  private HealthScore buildHealthScore(Entry<Long, List<GitHubEvent>> entry) {
    log.debug("--start count");

    HealthScore healthScore = HealthScore.commonBuilder(this.context.getMetricGroup())
        .repoId(entry.getKey()).numOfCommit(entry.getValue().size())
        .numOfDeveloper(countNumOfDeveloper(entry.getValue())).build();

    log.debug("--end count");

    double metricScore = 0.0;
    if (healthScore.getNumOfDeveloper() == 0) {
      skippedRepoIds.add(entry.getKey());
      return healthScore;
    } else {
      metricScore = (double) healthScore.getNumOfCommit() / healthScore.getNumOfDeveloper();
    }

    healthScore.getSingleMetricScores().put(this.metric, metricScore);

    return healthScore;
  }

  private int countNumOfDeveloper(List<GitHubEvent> events) {
    return events.parallelStream().map(GitHubEvent::getActor).map(Actor::getId)
        .collect(Collectors.toCollection(ConcurrentHashMap::newKeySet)).size();
  }

}
