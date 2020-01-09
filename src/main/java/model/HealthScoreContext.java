package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  private List<HealthScore> healthScores = new ArrayList<>();

  @Builder.Default
  private Map<Long, String> repoNames = new HashMap<>();

  private LocalDateTime dateTimeStart;

  private LocalDateTime dateTimeEnd;
  
  private Strategy metricGroup;

}
