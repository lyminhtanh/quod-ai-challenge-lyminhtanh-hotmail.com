package model;

import java.time.LocalDateTime;
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
  private ConcurrentMap<Long, HealthScore> healthScores = new ConcurrentHashMap<>();

  @Builder.Default
  private ConcurrentMap<Long, String> repoNames = new ConcurrentHashMap<>();

  private LocalDateTime dateTimeStart;

  private LocalDateTime dateTimeEnd;

  private Strategy strategy;

}
