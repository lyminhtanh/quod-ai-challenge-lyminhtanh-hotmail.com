package metric;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import enums.GitHubEventType;
import enums.IssueState;
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
public class AverageOpenedToClosedIssueHeathMetric implements HealthMetric, Command {

  private static final Metric METRIC = Metric.average_opened_to_closed_issue_ratio;

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

    Map<RepoIssue, IssueState> timeGroupedByRepoIssue = groupedByRepoIssue.entrySet().stream()
        .collect(Collectors.toMap(entry -> entry.getKey(), this::getIssueState));

    List<HealthScore> healthScores = timeGroupedByRepoIssue.entrySet().stream()
        .collect(Collectors.groupingBy(entry -> entry.getKey().getRepoId())).entrySet().stream()
        .map(entry -> calculateHealthScore(entry))
        .collect(Collectors.toList());

    return healthScores;
  }

  /**
   * @param entry
   * @return
   */
  private HealthScore calculateHealthScore(
      Map.Entry<Long, List<Map.Entry<RepoIssue, IssueState>>> entry) {
    double score = calculateHealthScore(entry.getValue());

    return buildHealthScore(entry.getKey(), score);
  }

  private HealthScore buildHealthScore(Long repoId, double score) {
    HealthScore healthScore = HealthScore.commonBuilder(this.context.getMetricGroup())
        .repoId(repoId).build();

    healthScore.getSingleMetricScores().put(METRIC, score);

    return healthScore;

  }

  /**
   * @param entry
   * @return
   */
  private IssueState getIssueState(Map.Entry<RepoIssue, List<GitHubEvent>> entry) {
    boolean isClosed = entry.getValue().stream().map(GitHubEvent::getPayload).map(Payload::getIssue)
        .map(Issue::getState).anyMatch(IssueState.CLOSED.name()::equalsIgnoreCase);

    return isClosed ? IssueState.CLOSED : IssueState.OPENED;
  }

  private RepoIssue buildRepoIssueKey(GitHubEvent event) {
    return new RepoIssue(event.getRepo().getId(), event.getPayload().getIssue().getId());
  }

  /**
   * calculate health score for each project
   *
   * @return List<HealthScore> odered descending by score
   */
  private double calculateHealthScore(List<Map.Entry<RepoIssue, IssueState>> entries) {
    if (entries.size() == 0) {
      return 0;
    }
    long numOfClosedIssue =
        entries.stream().map(Map.Entry::getValue).filter(IssueState.CLOSED::equals).count();

    if (numOfClosedIssue == 0) {
      return Double.MAX_VALUE;
    }
    
    return (double)(entries.size() - numOfClosedIssue)/numOfClosedIssue;
  }

}
