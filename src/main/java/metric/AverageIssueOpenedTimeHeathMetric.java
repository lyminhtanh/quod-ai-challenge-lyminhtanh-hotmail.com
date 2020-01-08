package metric;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import enums.Action;
import enums.GitHubEventType;
import enums.Metric;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.GitHubEvent;
import model.HealthScore;
import model.HealthScoreContext;
import model.Issue;
import model.Payload;
import model.Repo;
import model.RepoIssue;
import util.ChainUtil;
import util.FileUtil;

/**
 * Average time that an issue remains opened healthRatio = total(PushEvent of
 * project A)/total(PushEvent)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AverageIssueOpenedTimeHeathMetric implements HealthMetric, Command {

  private static final Metric METRIC = Metric.average_issue_opened_time;

  private HealthScoreContext context;

  @Override
  public boolean execute(Context context) throws Exception {
    this.context = (HealthScoreContext) context;


    List<HealthScore> currentMetricHealthScores = calculate(((HealthScoreContext) context));
    List<HealthScore> ctxHealthScores = ((HealthScoreContext) context).getHealthScores();

    ChainUtil.mergeHealthScores(ctxHealthScores, currentMetricHealthScores, METRIC);

    return false;
  }

  @Override
  public List<HealthScore> calculate(HealthScoreContext context) {

    List<String> lines = new ArrayList<>();
    for (String filePath : FileUtil.listJsonFiles()) {
      lines.addAll(FileUtil.readLinesByEventType(filePath, GitHubEventType.ISSUE_EVENT));
    }

    List<GitHubEvent> events =
        lines.stream().map(GitHubEvent::fromJson).collect(Collectors.toList());

    Map<Long, String> repoNames = events.parallelStream().map(GitHubEvent::getRepo)
        .collect(Collectors.toMap(Repo::getId, Repo::getName, (r1, r2) -> r1));

    this.context.getRepoNames().putAll(repoNames);

    Map<RepoIssue, List<GitHubEvent>> groupedByRepoIssue =
        events.stream().collect(Collectors.groupingBy(this::buildRepoIssueKey));

    groupedByRepoIssue.values().stream().filter(event -> event.size() > 1).forEach(event -> events
        .sort(Comparator.comparing(GitHubEvent::getCreatedAt, Comparator.naturalOrder())));

    Map<RepoIssue, Long> timeGroupedByRepoIssue = groupedByRepoIssue.entrySet().stream()
        .collect(Collectors.toMap(entry -> entry.getKey(), this::calculateOpenTimeInMinutes));

    List<HealthScore> healthScores = timeGroupedByRepoIssue.entrySet().stream()
        .collect(Collectors.groupingBy(entry -> entry.getKey().getRepoId())).entrySet().stream()
        .map(entry -> calculateHealthScore(entry))
        .sorted(Comparator.comparing(HealthScore::getScore, Comparator.reverseOrder()))
        .collect(Collectors.toList());

    return healthScores;
  }

  /**
   * @param entry
   * @return
   */
  private HealthScore calculateHealthScore(
      Map.Entry<Long, List<Map.Entry<RepoIssue, Long>>> entry) {
    double score = calculateHealthScore(entry.getValue());

    return buildHealthScore(entry.getKey(), score);
  }

  private HealthScore buildHealthScore(Long repoId, double score) {
    HealthScore healthScore = HealthScore.commonBuilder(this.context.getMetricGroup())
        .repoId(repoId).score(score).build();

    healthScore.getSingleMetricScores().put(METRIC, score);

    return healthScore;

  }

  /**
   * @param entry
   * @return
   */
  private Long calculateOpenTimeInMinutes(Map.Entry<RepoIssue, List<GitHubEvent>> entry) {
    final LocalDateTime openedAt = Optional.ofNullable(entry.getValue().get(0))
        .map(GitHubEvent::getPayload).map(Payload::getIssue).map(Issue::getCreatedAt)
        .orElseThrow(() -> new IllegalStateException(
            String.format("No created_at found for Issue: %d", entry.getKey().getIssueId())));

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

  private RepoIssue buildRepoIssueKey(GitHubEvent event) {
    return new RepoIssue(event.getRepo().getId(), event.getPayload().getIssue().getId());
  }

  /**
   * calculate health score for each project
   *
   * @return List<HealthScore> odered descending by score
   */
  private double calculateHealthScore(List<Map.Entry<RepoIssue, Long>> entries) {
    if (entries.size() == 0) {
      return 0;
    }
    Long avgOpenTime =
        entries.stream().map(Map.Entry::getValue).mapToLong(Long::longValue).sum() / entries.size();
    Long minOpenTime =
        entries.stream().map(Map.Entry::getValue).mapToLong(Long::longValue).min().getAsLong();
    if (minOpenTime == 0) {
      return 0;
    }
    return (double) avgOpenTime / minOpenTime;
  }

}
