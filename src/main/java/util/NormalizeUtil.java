package util;

public class NormalizeUtil {
    private final double dataHigh;
    private final double dataLow;
    private final double normalizedHigh;
    private final double normalizedLow;

    public NormalizeUtil(double dataHigh, double dataLow, double normalizedHigh, double normalizedLow) {
        this.dataHigh = dataHigh;
        this.dataLow = dataLow;
        this.normalizedHigh = normalizedHigh;
        this.normalizedLow = normalizedLow;
    }

    /**
     * Normalize x.
     * @param x The value to be normalized.
     * @return The result of the normalization.
     */
    public double normalize(double x) {
        return ((x - dataLow)
                / (dataHigh - dataLow))
                * (normalizedHigh - normalizedLow) + normalizedLow;
    }
}
