package metric;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import constant.Constant;
import enums.GitHubEventType;
import enums.Metric;
import enums.StatisticData;
import lombok.extern.log4j.Log4j2;
import model.GitHubEvent;
import model.HealthScore;
import model.HealthScoreContext;
import model.Repo;
import util.FileUtil;

@Log4j2
public abstract class HealthMetric implements Command {

  /**
   * calculate health score for one repo
   * 
   * @param List<GitHubEvent>
   * @return double
   */
  abstract protected double calculateHealthScore(List<GitHubEvent> repoEvents);

  protected HealthScoreContext context;

  protected Metric metric;

  protected ConcurrentMap<Long, List<GitHubEvent>> eventsByRepoId;

  protected ConcurrentMap<Long, ConcurrentMap<StatisticData, Number>> statisticDatas =
      new ConcurrentHashMap<>();

  public HealthMetric(Metric metric) throws IOException {
    this.metric = metric;
  }

  @Override
  public boolean execute(Context context) throws Exception {
    log.info("=====================START {} =========================", this.metric.name());

    this.eventsByRepoId = getEvents(this.metric.getType());
    this.context = ((HealthScoreContext) context);

    ConcurrentMap<Long, HealthScore> currentMetricHealthScores = calculate();
    ConcurrentMap<Long, HealthScore> ctxHealthScores = this.context.getHealthScores();

    mergeHealthScores(ctxHealthScores, currentMetricHealthScores);

    updateRepoNameMap();
    log.info("===================== END {} =========================", this.metric.name());
    return false;
  }

  /**
   * @param entry
   * @return
   */
  protected HealthScore calculateHealthScore(Map.Entry<Long, List<GitHubEvent>> entry) {
    double score = calculateHealthScore(entry.getValue());

    if (Constant.SKIP_SCORE == score) {
      return null;
    }
    return buildHealthScore(entry.getKey(), score);
  }

  /**
   * build HealthScore object
   * 
   * @param repoId
   * @param score
   * @return
   */
  protected HealthScore buildHealthScore(Long repoId, double score) {
    HealthScore healthScore =
        HealthScore.commonBuilder(this.context.getStrategy())
        .repoId(repoId).score(score).build();

    // set statistic if any
    if (getRepoStatistics(repoId) != null) {
      healthScore.setStatisticData(getRepoStatistics(repoId));
    }
    healthScore.getSingleMetricScores().put(this.metric, score);

    return healthScore;

  }

  /**
   * 
   * @param repoEvents
   * @return
   */
  protected long getRepoId(List<GitHubEvent> repoEvents) {
    return repoEvents.get(0).getRepo().getId();
  }

  /**
   * get map of statistic data by repo
   * 
   * @param repoId
   * @return
   */
  protected ConcurrentMap<StatisticData, Number> getRepoStatistics(Long repoId) {
    if (this.statisticDatas.get(repoId) == null) {
      this.statisticDatas.put(repoId, new ConcurrentHashMap<>());
    }
    return statisticDatas.get(repoId);
  }

  /**
   * calculate and collect only non-null HeathScores
   * 
   * @return
   * @throws IOException
   */
  private ConcurrentMap<Long, HealthScore> calculate() throws IOException {
    ConcurrentMap<Long, HealthScore> healthScoresMap = eventsByRepoId.entrySet().parallelStream()
        .map(this::calculateHealthScore).filter(Objects::nonNull).collect(
            Collectors.toConcurrentMap(HealthScore::getRepoId, Function.identity(), (r1, r2) -> {
              log.warn("removed duplicate healthscore repo {}", r1);
              return r1;
            }));

    return healthScoresMap;
  };

  private ConcurrentMap<Long, List<GitHubEvent>> getEvents(GitHubEventType eventType)
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
    log.info("- Events count {} : {} ", this.metric.getType(), eventsMap.size());
    return eventsMap;

  }

  private void updateRepoNameMap() {
    ConcurrentMap<Long, String> repoNames =
        eventsByRepoId.values().parallelStream().flatMap(List::stream).map(GitHubEvent::getRepo)
            .collect(Collectors.toConcurrentMap(Repo::getId, Repo::getName, (r1, r2) -> r1));

    context.getRepoNames().putAll(repoNames);
  }

  private void mergeHealthScores(ConcurrentMap<Long, HealthScore> ctxHealthScores,
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
    // update score for current metric
    ctxHealthScore.getSingleMetricScores().put(metric,
        currentMetricHealthScore.getSingleMetricScores().get(metric));

    // update statData coresponding to current metric

    currentMetricHealthScore.getStatisticData()
    .keySet()
    .stream()
    .filter(this.metric.getStatDatas()::contains)
    .forEach(key -> 
      ctxHealthScore.getStatisticData().put(key, currentMetricHealthScore.getStatisticData().get(key))
    );
    
    return ctxHealthScore;
  }
}
