package metric;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import enums.GitHubEventType;
import model.GitHubEvent;
import model.HealthScore;
import model.HealthScoreContext;
import model.Repo;
import util.FileUtil;

/**
 * Average number of commits (push) per day (to any branch) healthRatio = total(PushEvent of project
 * A)/total(PushEvent)
 */
public class AverageCommitHeathMetric implements HealthMetric, Command {

  @Override
  public boolean execute(Context context) throws Exception {
    List<HealthScore> healthScores = calculate();
    ((HealthScoreContext) context).getHealthScores().addAll(healthScores);
    return false;
  }

  @Override
  public List<HealthScore> calculate() {
    List<String> lines = new ArrayList<>();

    for (String filePath : FileUtil.listJsonFiles()) {
      lines.addAll(FileUtil.readLinesByEventType(filePath, GitHubEventType.PUSH_EVENT));
    }

    return calculateHealthScore(lines);
  }

  /**
   * calculate health score for each project
   *
   * @param lines
   * @return List<HealthScore> odered descending by score
   */
  private List<HealthScore> calculateHealthScore(List<String> lines) {
    // collect number of commits for each repo
    List<HealthScore> healthScores = lines.stream().map(GitHubEvent::fromJson)
        .collect(Collectors.groupingBy(x -> x.getRepo())).entrySet().stream()
        .map(entry -> HealthScore.builder().repo((Repo) entry.getKey())
            .numOfCommit(entry.getValue().size()).build())
        .sorted(Comparator.comparing(HealthScore::getNumOfCommit, Comparator.reverseOrder()))
        .collect(Collectors.toList());

    // select repo has max number of commits
    double maxCommit =
        healthScores.stream().mapToDouble(HealthScore::getNumOfCommit).max().getAsDouble();

    // update score
    healthScores
        .forEach(healthScore -> healthScore.setScore(healthScore.getNumOfCommit() / maxCommit));

    return healthScores;
  }

}
