package metric;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Collectors;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import enums.GitHubEventType;
import enums.Metric;
import model.GitHubEvent;
import model.HealthScore;
import model.HealthScoreContext;
import model.Repo;
import util.ChainUtil;
import util.FileUtil;

public abstract class HealthMetric implements Command {

  public HealthMetric(Metric metric, GitHubEventType eventType) {
    this.events = getEvents(eventType);
    this.metric = metric;
  }

  @Override
  public boolean execute(Context context) throws Exception {
    this.context = ((HealthScoreContext) context);

    List<HealthScore> currentMetricHealthScores = calculate();
    List<HealthScore> ctxHealthScores = ((HealthScoreContext) context).getHealthScores();

    ChainUtil.mergeHealthScores(ctxHealthScores, currentMetricHealthScores, this.metric);

    updateRepoNameMap();

    return false;
  }

  private void updateRepoNameMap() {
    Map<Long, String> repoNames = events.parallelStream().map(GitHubEvent::getRepo)
        .collect(Collectors.toMap(Repo::getId, Repo::getName, (r1, r2) -> r1));

    context.getRepoNames().putAll(repoNames);
  }

  abstract protected List<HealthScore> calculate() throws IOException;

  protected HealthScoreContext context;

  protected Metric metric;

  protected List<GitHubEvent> events;

  protected List<GitHubEvent> getEvents(GitHubEventType eventType) {
    List<GitHubEvent> events = new Vector<>();
    for (String filePath : FileUtil.listJsonFiles()) {
      List<GitHubEvent> readLinesByEventType =
          FileUtil.readLinesByEventType(filePath, eventType).parallelStream()
              .map(GitHubEvent::fromJson).collect(Collectors.toCollection(Vector::new));
      events.addAll(readLinesByEventType);
    }
    return events;

  }

}
