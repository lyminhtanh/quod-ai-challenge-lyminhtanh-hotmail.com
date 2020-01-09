package metric;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import enums.GitHubEventType;
import enums.Metric;
import model.GitHubEvent;
import model.HealthScore;
import model.Payload;
import model.Release;

/**
 * Average number of commits (push) per day (to any branch) healthRatio =
 * total(PushEvent of project A)/total(PushEvent)
 */
public class NumOfReleaseHeathMetric extends HealthMetric {

  public NumOfReleaseHeathMetric() throws IOException {
    super(Metric.num_of_releases, GitHubEventType.RELEASE_EVENT);
  }

  @Override
  public List<HealthScore> calculate() throws IOException {

    List<HealthScore> healthScores =
        events.stream().collect(Collectors.groupingBy(x -> x.getRepo().getId())).entrySet().stream()
            .map(this::buildHealthScore).collect(Collectors.toList());

    return healthScores;
  }

  private HealthScore buildHealthScore(Entry<Long, List<GitHubEvent>> entry) {
    HealthScore healthScore =
        HealthScore.commonBuilder(this.context.getMetricGroup()).repoId(entry.getKey())
        .numOfRelease(countRelease(entry.getValue())).build();

    healthScore.getSingleMetricScores().put(this.metric, (double) healthScore.getNumOfRelease());

    return healthScore;

  }

  private Integer countRelease(List<GitHubEvent> events) {
    return events.stream().map(GitHubEvent::getPayload).map(Payload::getRelease)
        .filter(Objects::nonNull).map(Release::getId)
        .collect(Collectors.toSet()).size();
  }

}
