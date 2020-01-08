package enums;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;

public enum MetricGroup {
	all_metric(Arrays.asList(Metric.average_commit, Metric.average_issue_opened_time));

	@Getter
	private List<Metric> metrics;

	private MetricGroup(List<Metric> metrics) {
		this.metrics = metrics;
	}
}
