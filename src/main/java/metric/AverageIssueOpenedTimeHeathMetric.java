package metric;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import constant.Constant;
import enums.Action;
import enums.GitHubEventType;
import enums.Metric;
import lombok.extern.log4j.Log4j2;
import model.GitHubEvent;
import model.HealthScore;
import model.Issue;
import model.Payload;

/**
 * Average time that an issue remains opened healthRatio = total(PushEvent of
 * project A)/total(PushEvent)
 */
@Log4j2
public class AverageIssueOpenedTimeHeathMetric extends HealthMetric {

  public AverageIssueOpenedTimeHeathMetric() throws IOException {
    super(Metric.average_issue_opened_time, GitHubEventType.ISSUE_EVENT);
  }

  /**
   * @param entry
   * @return
   */
  @Override
  protected HealthScore calculateHealthScore(
      Map.Entry<Long, List<GitHubEvent>> entry) {
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

    ConcurrentMap<Long, List<GitHubEvent>> groupedByIssueId =

        repoEvents.parallelStream().collect(
            Collectors.groupingByConcurrent(event -> event.getPayload().getIssue().getId()));

    groupedByIssueId.values().parallelStream().filter(event -> event.size() > 1)
        .forEach(event -> event
            .sort(Comparator.comparing(GitHubEvent::getCreatedAt, Comparator.naturalOrder())));

    ConcurrentMap<Long, Long> timeGroupedByIssueId =
        groupedByIssueId.entrySet().parallelStream().collect(
            Collectors.toConcurrentMap(entry -> entry.getKey(), this::calculateOpenTimeInMinutes));

    return calculateHealthScore(timeGroupedByIssueId.values());
  }

  private double calculateHealthScore(Collection<Long> issueOpenTimes) {
    if (issueOpenTimes.size() == 0) {
      return Constant.SKIP_SCORE;
    }

    Long avgOpenTime =
        issueOpenTimes.stream().mapToLong(Long::longValue).sum() / issueOpenTimes.size();

    Long minOpenTime = issueOpenTimes.stream().mapToLong(Long::longValue).min().getAsLong();

    if (minOpenTime == 0) {
      return Constant.SKIP_SCORE;
    }
    return (double) avgOpenTime / minOpenTime;
  }

  /**
   * @param entry
   * @return
   */
  private Long calculateOpenTimeInMinutes(Map.Entry<Long, List<GitHubEvent>> entry) {
    final LocalDateTime openedAt = Optional.ofNullable(entry.getValue().get(0))
        .map(GitHubEvent::getPayload).map(Payload::getIssue).map(Issue::getCreatedAt)
        .orElseThrow(() -> new IllegalStateException(
            String.format("No created_at found for Issue", entry.getKey())));

    final LocalDateTime nonOpenedAt = getNonOpenActionCreatedAt(entry.getValue());
    Duration duration = Duration.between(openedAt, nonOpenedAt);

    return duration.toMinutes();
  }

  private LocalDateTime getNonOpenActionCreatedAt(List<GitHubEvent> events) {
    boolean hasOpenAction = hasOpenedAction(events);

    int nonOpenedIndex = 0;

    if (hasOpenAction) {
      if (events.size() == 1) {
        return this.context.getDateTimeEnd().plusHours(1);
      }
      nonOpenedIndex = 1;
    }

    return events.get(nonOpenedIndex).getCreatedAt();

  }

  private boolean hasOpenedAction(List<GitHubEvent> events) {
    return events.stream().map(GitHubEvent::getPayload).map(Payload::getAction)
        .anyMatch(Action.OPENED.value()::equals);
  }



}
