package metric;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import enums.GitHubEventType;
import enums.Metric;
import model.Actor;
import model.GitHubEvent;
import model.HealthScore;

/**
 * Ratio of commit per developers
 */
public class AverageCommitPerDeveloperRatioHeathMetric extends HealthMetric {

  public AverageCommitPerDeveloperRatioHeathMetric() throws IOException {
    super(Metric.average_commit_per_developer_ratio, GitHubEventType.PUSH_EVENT);
  }

  @Override
  public List<HealthScore> calculate() throws IOException {

    List<HealthScore> healthScores =
        events.stream().collect(Collectors.groupingBy(x -> x.getRepo().getId())).entrySet().stream()
            .map(this::buildHealthScore)
            .sorted(Comparator.comparing(HealthScore::getNumOfCommit, Comparator.reverseOrder()))
            .collect(Collectors.toList());

    return healthScores;
  }

  private HealthScore buildHealthScore(Entry<Long, List<GitHubEvent>> entry) {
    HealthScore healthScore = HealthScore.commonBuilder(this.context.getMetricGroup())
        .repoId(entry.getKey())
        .numOfCommit(entry.getValue().size()).numOfDeveloper(countNumOfDeveloper(entry.getValue()))
        .build();

    healthScore.getSingleMetricScores().put(this.metric,
        (double) healthScore.getNumOfCommit() / healthScore.getNumOfDeveloper());

    return healthScore;
  }

  private Integer countNumOfDeveloper(List<GitHubEvent> events) {
    return events.stream().map(GitHubEvent::getActor).map(Actor::getId).collect(Collectors.toSet())
        .size();
  }




}
