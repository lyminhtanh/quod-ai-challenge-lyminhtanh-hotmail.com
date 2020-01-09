package enums;

import java.util.Arrays;
import java.util.List;

import lombok.Getter;

public enum MetricGroup {
  ALL_METRIC(
      Arrays.asList(Metric.values()));

	@Getter
	private List<Metric> metrics;

	private MetricGroup(List<Metric> metrics) {
		this.metrics = metrics;
	}
}
