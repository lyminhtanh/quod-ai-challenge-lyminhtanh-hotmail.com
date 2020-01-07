package model;

import java.util.List;

import org.apache.commons.chain.impl.ContextBase;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HealthScoreContext extends ContextBase {

  /**
   *
   */
  private static final long serialVersionUID = 1035495750880405943L;

  private List<HealthScore> healthScores;
}
