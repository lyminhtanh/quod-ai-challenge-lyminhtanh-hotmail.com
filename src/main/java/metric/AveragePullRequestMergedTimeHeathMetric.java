package metric;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import constant.Constant;
import enums.Metric;
import enums.StatisticData;
import model.GitHubEvent;
import model.Payload;
import model.PullRequest;
import util.DateTimeUtil;

/**
 * Average time for a pull request to get merged If an issue has not been merged yet in the search
 * period, it will be ignored in calculation
 */
public class AveragePullRequestMergedTimeHeathMetric extends HealthMetric {

  public AveragePullRequestMergedTimeHeathMetric() throws IOException {
    super(Metric.AVERAGE_PULL_REQUEST_MERGE_TIME);
  }

  /**
   * calculate health score for one repo
   * 
   * @param List<GitHubEvent> odered ascending by issueCreatedAt
   * @return double
   */
  @Override
  protected double calculateHealthScore(List<GitHubEvent> repoEvents) {

    ConcurrentMap<Long, List<GitHubEvent>> groupedByPullRequestId =
        repoEvents.parallelStream().collect(
            Collectors.groupingByConcurrent(event -> event.getPayload().getPullRequest().getId()));

    ConcurrentMap<Long, Long> timeGroupedByPullRequestId =
        groupedByPullRequestId.entrySet().parallelStream().collect(
            Collectors.toConcurrentMap(entry -> entry.getKey(), this::calculateMergingTimeInSecond));

    Long repoId = getRepoId(repoEvents);

    // update statistics
    getRepoStatistics(repoId).put(StatisticData.NUM_OF_PULL_REQUESTS,
        timeGroupedByPullRequestId.size());

    return calculateHealthScore(timeGroupedByPullRequestId.values(), repoId);
  }


  /**
   * @param entry
   * @return
   */
  private Long calculateMergingTimeInSecond(Map.Entry<Long, List<GitHubEvent>> entry) {

    final LocalDateTime openedAt = Optional.ofNullable(entry.getValue().get(0))
        .map(GitHubEvent::getPayload).map(Payload::getPullRequest).map(PullRequest::getCreatedAt)
        .orElseThrow(() -> new IllegalStateException(
            String.format("No created_at found for PullRequest: %d", entry.getKey())));

    final LocalDateTime mergedAt = getMergedAt(entry.getValue());

    // the PR has not been merged yet
    if (mergedAt == null) {
      return Constant.SKIP_LONG_VALUE;
    }

    Duration duration = Duration.between(openedAt, mergedAt);

    return duration.getSeconds();
  }

  private LocalDateTime getMergedAt(List<GitHubEvent> events) {

    return events.parallelStream().map(GitHubEvent::getPayload).map(Payload::getPullRequest)
        .map(PullRequest::getMergedAt).filter(Objects::nonNull).findAny().orElse(null);

  }

  /**
   * calculate health score = avgMergedTime / minMergedTime
   * 
   * @param repoId
   *
   * @return List<HealthScore> odered descending by score
   */
  private double calculateHealthScore(Collection<Long> mergedTimes, Long repoId) {

    List<Long> validMergedTimes = mergedTimes.stream().filter(DateTimeUtil::isValidTime)
        .collect(Collectors.toCollection(Vector::new));

    long sum = validMergedTimes.parallelStream().mapToLong(Long::longValue).sum();

    long count = validMergedTimes.parallelStream().count();

    if (count == 0) {
      return Constant.SKIP_SCORE;
    }

    double avgMergedTime = (double) sum / count;

    Long minMergedTime =
        validMergedTimes.parallelStream().mapToLong(Long::longValue).min().getAsLong();

    if (minMergedTime == 0) {
      return Constant.SKIP_SCORE;
    }

    // update statistic
    getRepoStatistics(repoId).put(StatisticData.ISSUE_AVERAGE_MERGING_TIME_IN_SECOND,
        avgMergedTime);
    getRepoStatistics(repoId).put(StatisticData.ISSUE_MIN_MERGING_TIME_IN_SECOND,
        minMergedTime);

    return (double) avgMergedTime / minMergedTime;
  }

}
