package metric;

import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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

  protected HealthScoreContext context;

  protected Metric metric;

  protected ConcurrentMap<Long, List<GitHubEvent>> events;

  protected List<Long> skippedRepoIds;

  private GitHubEventType eventType;

  public HealthMetric(Metric metric, GitHubEventType eventType) throws IOException {
    this.metric = metric;
    this.eventType = eventType;
  }

  @Override
  public boolean execute(Context context) throws Exception {
    log.info("-- START {}", this.metric.name());
    skippedRepoIds = new Vector<>();

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
    ConcurrentMap<Long, String> repoNames =
        events.values().parallelStream().flatMap(List::stream).map(GitHubEvent::getRepo)
            .collect(Collectors.toConcurrentMap(Repo::getId, Repo::getName, (r1, r2) -> r1));

    context.getRepoNames().putAll(repoNames);
  }


  protected ConcurrentMap<Long, List<GitHubEvent>> getEvents(GitHubEventType eventType)
      throws Exception {
    ConcurrentMap<Long, List<GitHubEvent>> eventsMap = new ConcurrentHashMap<>();

    FileUtil.listJsonFiles().parallelStream().forEach(filePath -> {
      ConcurrentMap<Long, List<GitHubEvent>> readLinesByEventType;
      try {
        readLinesByEventType = FileUtil.readLinesByEventType(filePath, eventType);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      eventsMap.putAll(readLinesByEventType);
    });

    // events.removeIf(event -> !eventType.value().equals(event.getType()));
    log.info("- Events count {} : {} ", this.eventType, events.size());
    return eventsMap;

  }

}
