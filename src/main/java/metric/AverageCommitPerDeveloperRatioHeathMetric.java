package metric;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import constant.Constant;
import enums.Metric;
import enums.StatisticData;
import lombok.extern.log4j.Log4j2;
import model.Actor;
import model.GitHubEvent;

/**
 * Ratio of commits per developers
 */
@Log4j2
public class AverageCommitPerDeveloperRatioHeathMetric extends HealthMetric {

  public AverageCommitPerDeveloperRatioHeathMetric() throws IOException {
    super(Metric.AVERAGE_COMMITS_PER_DEVELOPERS_RATIO);
  }

  @Override
  protected double calculateHealthScore(List<GitHubEvent> repoEvents) {
    final int numOfCommits = repoEvents.size();

    final int numOfDevelopers = countNumOfDeveloper(repoEvents);

    final long repoId = getRepoId(repoEvents);

    // handle exception
    if (numOfDevelopers == 0) {
      log.warn("!! numOfDevelopers should not be 0. {}", repoEvents);

      log.warn("========================== Skip this repo {} ==============================",
          repoId);

      return Constant.SKIP_SCORE;
    }

    // update statistic data
    getRepoStatistics(repoId).put(StatisticData.NUM_OF_COMMITS, numOfCommits);
    getRepoStatistics(repoId).put(StatisticData.NUM_OF_DEVELOPERS, numOfDevelopers);

    return (double) numOfCommits / numOfDevelopers;
  }

  private int countNumOfDeveloper(List<GitHubEvent> events) {
    return events.parallelStream().map(GitHubEvent::getActor).map(Actor::getId)
        .collect(Collectors.toCollection(ConcurrentHashMap::newKeySet)).size();
  }

}
