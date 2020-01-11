package metric;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import constant.Constant;
import enums.Action;
import enums.Metric;
import enums.StatisticData;
import lombok.extern.log4j.Log4j2;
import model.GitHubEvent;
import model.Issue;
import model.Payload;
import util.DateTimeUtil;

/**
 * Average time that an issue remains opening healthRatio = total(PushEvent of project
 * A)/total(PushEvent)
 */
@Log4j2
public class AverageIssueOpenedTimeHeathMetric extends HealthMetric {

  public AverageIssueOpenedTimeHeathMetric() throws IOException {
    super(Metric.AVERAGE_ISSUE_OPEN_TIME);
  }

  /**
   * calculate health score for one repo
   * 
   * @param List<GitHubEvent>
   * @return double
   */
  @Override
  protected double calculateHealthScore(List<GitHubEvent> repoEvents) {

    // group events by issue
    ConcurrentMap<Long, List<GitHubEvent>> groupedByIssueId =
        repoEvents.parallelStream().collect(
            Collectors.groupingByConcurrent(event -> event.getPayload().getIssue().getId()));

    // calculate open time for all issues of current repo
    ConcurrentMap<Long, Long> timeGroupedByIssueId =
        groupedByIssueId.entrySet().parallelStream().collect(
            Collectors.toConcurrentMap(entry -> entry.getKey(), this::calculateOpenTimeInSecond));

    final long repoId = repoEvents.get(0).getRepo().getId();
    return calculateHealthScore(timeGroupedByIssueId.values(), repoId);
  }

  /**
   * 
   * @param issueOpenTimes
   * @param repoId
   * @return
   */
  private double calculateHealthScore(Collection<Long> issueOpenTimes, long repoId) {
    List<Long> validTimes = issueOpenTimes.stream().filter(DateTimeUtil::isValidTime)
        .collect(Collectors.toCollection(Vector::new));
    
    if (validTimes.size() == 0) {
      return Constant.SKIP_SCORE;
    }

    Long avgOpenTime =
        validTimes.stream().mapToLong(Long::longValue).sum() / validTimes.size();

    Long minOpenTime = validTimes.stream().mapToLong(Long::longValue).min().getAsLong();

    if (minOpenTime == 0) {
      return Constant.SKIP_SCORE;
    }

    // update statistic data
    getRepoStatistics(repoId).put(StatisticData.ISSUE_AVERAGE_OPEN_TIME_IN_SECOND, avgOpenTime);
    getRepoStatistics(repoId).put(StatisticData.ISSUE_MIN_MERGING_TIME_IN_SECOND, minOpenTime);

    
    return (double) avgOpenTime / minOpenTime;
  }

  /**
   * calculate OpenTime In Second for one issue
   * 
   * @param entry
   * @return
   */
  private Long calculateOpenTimeInSecond(Map.Entry<Long, List<GitHubEvent>> entry) {

    List<GitHubEvent> events = entry.getValue();

    final LocalDateTime openedIssueCreatedAt =
        Optional.ofNullable(events.get(0))
        .map(GitHubEvent::getPayload).map(Payload::getIssue).map(Issue::getCreatedAt)
        .orElseThrow(() -> new IllegalStateException(
            String.format("No created_at found for Issue", entry.getKey())));

    final LocalDateTime nonOpenedCreatedAt = getNonOpenActionCreatedAt(events);

    if (openedIssueCreatedAt == null || nonOpenedCreatedAt == null
        || openedIssueCreatedAt.isAfter(nonOpenedCreatedAt)) {
      log.warn(
          "Can not calculate duration between openedIssueCreatedAt and nonOpenedCreatedAt. Skip issue. {}",
          events);

      return Constant.SKIP_LONG_VALUE; // TODO
    }

    Duration duration = Duration.between(openedIssueCreatedAt, nonOpenedCreatedAt);

    return duration.getSeconds();
  }

  /**
   * get time that change from an opened issue to another state (blocked, merged, ...) events must
   * have at least 2 elements and sorted
   * 
   * @param events
   * @return
   */
  private LocalDateTime getNonOpenActionCreatedAt(List<GitHubEvent> events) {
    // sort ascending by issue createdAt
    if (events.size() > 1) {
      events.sort(Comparator.comparing(GitHubEvent::getCreatedAt, Comparator.naturalOrder()));
    }

    if (hasOpenedAction(events)) {
      if (events.size() == 1) {
        // first value is opened action
        // this issue is not closed until the end of input range. Set as dateTimeEnd
        return this.context.getDateTimeEnd().plusHours(1);
      } else {
        // second event should be nearest non-open action
        return events.get(1).getCreatedAt();
      }
    }
    // the issue is opened before, first event should be non-opened action
    return events.get(0).getCreatedAt();

  }

  /**
   * check if there is OPENED action for a certain issue in the input range
   * 
   * @param events
   * @return
   */
  private boolean hasOpenedAction(List<GitHubEvent> events) {
    return events.stream().map(GitHubEvent::getPayload).map(Payload::getAction)
        .anyMatch(Action.OPENED.value()::equals);
  }

}
