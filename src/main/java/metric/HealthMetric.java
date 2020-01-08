package metric;

import java.util.List;

import model.HealthScore;
import model.HealthScoreContext;

public interface HealthMetric {
  List<HealthScore> calculate(HealthScoreContext context);
}
