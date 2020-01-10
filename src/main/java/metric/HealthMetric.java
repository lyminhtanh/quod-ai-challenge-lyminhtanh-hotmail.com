package metric;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import enums.GitHubEventType;
import enums.Metric;
import lombok.extern.log4j.Log4j2;
import model.GitHubEvent;
import model.HealthScore;
import model.HealthScoreContext;
import model.Repo;
import util.ChainUtil;
import util.FileUtil;

@Log4j2
public abstract class HealthMetric implements Command {

  abstract protected List<HealthScore> calculate() throws IOException;

  private GitHubEventType eventType;

  public HealthMetric(Metric metric, GitHubEventType eventType) throws IOException {
    this.metric = metric;
    this.eventType = eventType;
  }

  @Override
  public boolean execute(Context context) throws Exception {
    log.info("-- START {}", this.metric.name());
    this.events = getEvents(eventType);
    this.context = ((HealthScoreContext) context);

    List<HealthScore> currentMetricHealthScores = calculate();
    List<HealthScore> ctxHealthScores = ((HealthScoreContext) context).getHealthScores();

    ChainUtil.mergeHealthScores(ctxHealthScores, currentMetricHealthScores, this.metric);

    updateRepoNameMap();
    log.info("-- END {}", this.metric.name());
    return false;
  }

  private void updateRepoNameMap() {
    Map<Long, String> repoNames = events.parallelStream().map(GitHubEvent::getRepo)
        .collect(Collectors.toMap(Repo::getId, Repo::getName, (r1, r2) -> r1));

    context.getRepoNames().putAll(repoNames);
  }

  protected HealthScoreContext context;

  protected Metric metric;

  protected List<GitHubEvent> events;

  protected List<GitHubEvent> getEvents(GitHubEventType eventType) throws Exception {
    List<GitHubEvent> events = new Vector<>();

    FileUtil.listJsonFiles().parallelStream().forEach(filePath -> {
      List<GitHubEvent> readLinesByEventType;
      try {
        readLinesByEventType = FileUtil.readLinesByEventType(filePath, eventType).parallelStream()
            .map(this::parseGitHubEvent).filter(Objects::nonNull)
            .collect(Collectors.toCollection(Vector::new));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      events.addAll(readLinesByEventType);
    });

    events.removeIf(event -> !eventType.value().equals(event.getType()));
    log.info("- Events count {} : {} ", this.eventType, events.size());
    return events;

  }

  private GitHubEvent parseGitHubEvent(String t) {
    try {
      return GitHubEvent.fromJson(t);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
