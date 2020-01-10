package metric;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import enums.GitHubEventType;
import enums.Metric;
import lombok.extern.log4j.Log4j2;
import model.GitHubEvent;
import model.HealthScore;
import model.HealthScoreContext;
import model.Repo;
import util.FileUtil;

@Log4j2
public abstract class HealthMetric implements Command {

  abstract protected HealthScore calculateHealthScore(Map.Entry<Long, List<GitHubEvent>> entry);

  protected ConcurrentMap<Long, HealthScore> calculate() throws IOException {
    ConcurrentMap<Long, HealthScore> healthScoresMap =
        events.entrySet().parallelStream().map(this::calculateHealthScore).collect(
            Collectors.toConcurrentMap(HealthScore::getRepoId, Function.identity(), (r1, r2) -> {
              log.warn("removed duplicate healthscore repo {}", r1);
              return r1;
            }));

    return healthScoresMap;
  };


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

    ConcurrentMap<Long, HealthScore> currentMetricHealthScores = calculate();
    ConcurrentMap<Long, HealthScore> ctxHealthScores = this.context.getHealthScores();

    mergeHealthScores(ctxHealthScores, currentMetricHealthScores);

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
    log.info("- Events count {} : {} ", this.eventType, eventsMap.size());
    return eventsMap;

  }

  protected HealthScore buildHealthScore(Long repoId, double score) {
    HealthScore healthScore = HealthScore.commonBuilder(this.context.getMetricGroup())
        .repoId(repoId).score(score).build();

    healthScore.getSingleMetricScores().put(this.metric, score);

    return healthScore;

  }

  public void mergeHealthScores(ConcurrentMap<Long, HealthScore> ctxHealthScores,
      ConcurrentMap<Long, HealthScore> currentMetricHealthScores) {

    ConcurrentHashMap<Long, HealthScore> mergedHealthScoreMap = Stream
        .of(ctxHealthScores, currentMetricHealthScores).flatMap(map -> map.entrySet().stream())
        .collect(Collectors.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue,
            this::mergeHealthScore, ConcurrentHashMap::new));

    // update merged result to context
    context.setHealthScores(mergedHealthScoreMap);
  }

  private HealthScore mergeHealthScore(HealthScore ctxHealthScore,
      HealthScore currentMetricHealthScore) {
    ctxHealthScore.getSingleMetricScores().put(metric,
        currentMetricHealthScore.getSingleMetricScores().get(metric));
    return ctxHealthScore;
  }

}
