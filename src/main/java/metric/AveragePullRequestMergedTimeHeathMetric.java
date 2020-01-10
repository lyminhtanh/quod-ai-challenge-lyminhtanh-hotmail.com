package metric;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import constant.Constant;
import enums.GitHubEventType;
import enums.Metric;
import model.GitHubEvent;
import model.HealthScore;
import model.Payload;
import model.PullRequest;

/**
 * Average time for a pull request to get merged If an issue has not yet merged in the search
 * period, it will be ignored in calculation
 */
public class AveragePullRequestMergedTimeHeathMetric extends HealthMetric {

  public AveragePullRequestMergedTimeHeathMetric() throws IOException {
    super(Metric.average_pull_request_merge_time, GitHubEventType.PULL_REQUEST_EVENT);
  }

  /**
   * @param entry
   * @return
   */
  @Override
  protected HealthScore calculateHealthScore(Map.Entry<Long, List<GitHubEvent>> entry) {
    double score = calculateHealthScore(entry.getValue());

    if (Constant.SKIP_SCORE == score) {
      skippedRepoIds.add(entry.getKey()); // or return null? TODO
    }
    return buildHealthScore(entry.getKey(), score);
  }

  /**
   * calculate health score for one repo
   * 
   * @param List<GitHubEvent> odered ascending by issueCreatedAt
   * @return double
   */
  private double calculateHealthScore(List<GitHubEvent> repoEvents) {

    ConcurrentMap<Long, List<GitHubEvent>> groupedByPullRequestId =

        repoEvents.parallelStream().collect(
            Collectors.groupingByConcurrent(event -> event.getPayload().getPullRequest().getId()));

    ConcurrentMap<Long, Long> timeGroupedByPullRequestId =
        groupedByPullRequestId.entrySet().parallelStream().collect(
            Collectors.toConcurrentMap(entry -> entry.getKey(), this::calculateOpenTimeInMinutes));

    return calculateHealthScore(timeGroupedByPullRequestId.values());
  }


  /**
   * @param entry
   * @return
   */
  private Long calculateOpenTimeInMinutes(Map.Entry<Long, List<GitHubEvent>> entry) {
    final LocalDateTime openedAt = Optional.ofNullable(entry.getValue().get(0))
        .map(GitHubEvent::getPayload).map(Payload::getPullRequest).map(PullRequest::getCreatedAt)
        .orElseThrow(() -> new IllegalStateException(String
            .format("No created_at found for PullRequest: %d", entry.getKey())));

    final LocalDateTime mergedAt = getMergedAt(entry.getValue());

    // the PR has not been merged yet
    if (mergedAt == null) {
      return Long.valueOf(-1);
    }

    Duration duration = Duration.between(openedAt, mergedAt);

    return duration.toMinutes();
  }

  private LocalDateTime getMergedAt(List<GitHubEvent> events) {

    return events.parallelStream().map(GitHubEvent::getPayload).map(Payload::getPullRequest)
        .map(PullRequest::getMergedAt).filter(Objects::nonNull).findAny()
        .orElse(null);

  }

  /**
   * calculate health score for each project
   *
   * @return List<HealthScore> odered descending by score
   */
  private double calculateHealthScore(Collection<Long> mergedTimes) {

    long sum = mergedTimes.parallelStream().mapToLong(Long::longValue)
        .filter(n -> n >= 0)
        .sum();
    long count = mergedTimes.parallelStream().filter(n -> n >= 0).count();

    if (count == 0) {
      return Constant.SKIP_SCORE;
    }

    Long avgMergedTime =
        sum / count;
    Long minMergedTime =
        mergedTimes.parallelStream().mapToLong(Long::longValue)
            .filter(n -> n >= 0)
            .min().getAsLong();

    if (minMergedTime == 0) {
      return Constant.SKIP_SCORE;
    }
    return (double) avgMergedTime / minMergedTime;
  }

}
