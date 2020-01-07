package metric;

import java.util.List;

import model.HealthScore;

public interface HealthMetric {
  List<HealthScore> calculate();
}
