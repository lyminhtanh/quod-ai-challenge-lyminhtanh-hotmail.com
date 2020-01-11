package metric;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import enums.Metric;
import enums.StatisticData;
import model.GitHubEvent;
import model.Payload;
import model.Release;

/**
 * Number of releases
 */
public class NumOfReleaseHeathMetric extends HealthMetric {

  public NumOfReleaseHeathMetric() throws IOException {
    super(Metric.NUMBER_OF_RELEASES);
  }

  @Override
  protected double calculateHealthScore(List<GitHubEvent> repoEvents) {
    final Integer countRelease = countRelease(repoEvents);

    // update statistic data
    getRepoStatistics(getRepoId(repoEvents)).put(StatisticData.NUM_OF_RELEASES, countRelease);

    return countRelease;
  }

  /**
   * count number of Releases for current repo
   * 
   * @param events
   * @return
   */
  private Integer countRelease(List<GitHubEvent> events) {
    return events.stream().map(GitHubEvent::getPayload).map(Payload::getRelease)
        .filter(Objects::nonNull).map(Release::getId).collect(Collectors.toSet()).size();
  }
}
