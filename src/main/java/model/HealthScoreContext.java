package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.chain.impl.ContextBase;

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

  private LocalDateTime dateTimeStart;

  private LocalDateTime dateTimeEnd;

}
