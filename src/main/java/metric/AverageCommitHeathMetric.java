package metric;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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
  public List<HealthScore> calculate() throws IOException {
    // collect number of commits for each repo
    List<HealthScore> healthScores =
        events.stream().collect(Collectors.groupingBy(x -> x.getRepo().getId())).entrySet().stream()
            .map(this::buildHealthScore)
            .sorted(Comparator.comparing(HealthScore::getNumOfCommit, Comparator.reverseOrder()))
            .collect(Collectors.toList());

    return healthScores;
  }

  private HealthScore buildHealthScore(Entry<Long, List<GitHubEvent>> entry) {
    HealthScore healthScore =
        HealthScore.commonBuilder(this.context.getMetricGroup()).repoId(entry.getKey())
        .numOfCommit(entry.getValue().size()).build();

    healthScore.getSingleMetricScores().put(this.metric, (double) healthScore.getNumOfCommit());

    return healthScore;
  }




}
