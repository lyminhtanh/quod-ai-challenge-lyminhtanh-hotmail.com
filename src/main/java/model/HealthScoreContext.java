package model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.chain.impl.ContextBase;

import enums.Strategy;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class HealthScoreContext extends ContextBase {
  /**
   *
   */
  private static final long serialVersionUID = 1035495750880405943L;

  @Builder.Default
  private List<HealthScore> healthScores = new Vector<>();

  @Builder.Default
  private ConcurrentMap<Long, HealthScore> healthScoresMap = new ConcurrentHashMap<>();

  @Builder.Default
  private ConcurrentMap<Long, String> repoNames = new ConcurrentHashMap<>();

  private LocalDateTime dateTimeStart;

  private LocalDateTime dateTimeEnd;

  private Strategy metricGroup;

}
