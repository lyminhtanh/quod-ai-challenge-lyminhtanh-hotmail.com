package util;

import java.util.Collection;

import org.apache.commons.collections4.CollectionUtils;

import enums.Metric;
import enums.Strategy;
import model.HealthScore;

public class NormalizeUtil {

  /**
   * normalize healthscores in range 0 and 1 for all single metrics
   * 
   * @param healthScores
   * @param metricGroup
   */
  public static void normalize(Collection<HealthScore> healthScores, Strategy metricGroup) {
    if (CollectionUtils.isEmpty(healthScores)) {
      return;
    }
    metricGroup.getMetrics().forEach(metric -> normalize(healthScores, metric));
  }

  /**
   * normalize healthscores in range 0 and 1
   * 
   * @param healthScores
   * @param metricGroup
   */
  public static void normalize(Collection<HealthScore> healthScores) {

    if (CollectionUtils.isEmpty(healthScores)) {
      return;
    }

    // process normalization for single metric
    final double dataHigh = healthScores.stream().map(HealthScore::getScore)
        .mapToDouble(Double::doubleValue).max().getAsDouble();

    final double dataLow = healthScores.stream().map(HealthScore::getScore)
        .mapToDouble(Double::doubleValue).min().getAsDouble();

    // can not normalize for all-zero list
    if (dataHigh - dataLow == 0.0) {
      return;
    }

    // update normalized score
    healthScores.forEach(healthScore -> {
      healthScore.setScore(normalize(healthScore.getScore(), dataHigh, dataLow));
    });
  }

  /**
   * normalize healthscores in range 0 and 1
   * 
   * @param healthScores
   * @param metricGroup
   */
  public static void normalize(Collection<HealthScore> healthScores, Metric metric) {

    // process normalization for single metric
    final double dataHigh = healthScores.stream().map(HealthScore::getSingleMetricScores)
        .mapToDouble(m -> m.get(metric)).max().getAsDouble();

    final double dataLow = healthScores.stream().map(HealthScore::getSingleMetricScores)
        .mapToDouble(m -> m.get(metric)).min().getAsDouble();

    // can not normalize for all-zero list
    if (dataHigh - dataLow == 0.0) {
      return;
    }

    healthScores.forEach(healthScore -> {
      Double originScore = healthScore.getSingleMetricScores().get(metric);

      // update normalized score
      healthScore.getSingleMetricScores().put(metric, normalize(originScore, dataHigh, dataLow));
    });
  }

  /**
   * Normalize value.
   * 
   * @param value The value to be normalized.
   * @return The result of the normalization.
   */
  private static double normalize(double value, final double dataHigh, final double dataLow) {

    return (value - dataLow) / (dataHigh - dataLow);
  }
}
