package enums;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;

public enum Strategy {
  ALL_METRIC(
      Arrays.asList(Metric.values()));

	@Getter
	private List<Metric> metrics;

	private Strategy(List<Metric> metrics) {
		this.metrics = metrics;
	}
}
