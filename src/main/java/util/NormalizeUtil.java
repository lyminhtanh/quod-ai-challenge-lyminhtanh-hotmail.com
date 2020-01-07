package util;

import model.HealthScore;

import java.util.List;

public class NormalizeUtil {

    /**
     * normalize healthscores in range 0 and 1
     * @param healthScores
     */
    public static void normalize(List<HealthScore> healthScores) {
        final double dataHigh = healthScores.stream()
                .mapToDouble(HealthScore::getScore)
                .max()
                .getAsDouble();

        final double dataLow = healthScores.stream()
                .mapToDouble(HealthScore::getScore)
                .min()
                .getAsDouble();

        healthScores.forEach(healthScore -> healthScore.setScore(normalize(healthScore.getScore(), dataHigh, dataLow)));
    }

    /**
     * Normalize value.
     * @param value The value to be normalized.
     * @return The result of the normalization.
     */
    private static double normalize(double value, final double dataHigh, final double dataLow) {
        return (value - dataLow)
                / (dataHigh - dataLow);
    }
}
