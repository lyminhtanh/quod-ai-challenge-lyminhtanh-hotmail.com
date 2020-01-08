package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import constant.Constant;
import enums.Action;
import enums.Metric;
import enums.MetricGroup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthScore {

	private Long repoId;

	private String repoName;

	private Integer numOfCommit;

	private Double avgCommitScore;

	private Double avgIssueOpenTimeScore;

	@Builder.Default
	private Map<Metric, Double> singleMetricScores = new HashMap<>();

	private Double score;

	public String[] toCsvRow() {
		List<String> singleMetricScoresStr = Stream.of(Metric.values()).map(singleMetricScores::get)
				.map(String::valueOf).collect(Collectors.toList());
		
		List<String> row = new ArrayList<>();

		row.add(String.valueOf(repoId));
		row.add(repoName);
		row.add(String.valueOf(score));
		row.add(String.valueOf(numOfCommit));
		row.addAll(singleMetricScoresStr);
		return row.toArray(new String[0]);
	};

	public static HealthScoreBuilder commonBuilder(MetricGroup metricGroup) {
		return HealthScore.builder().singleMetricScores(initSingleMetricMap(metricGroup));

	}

	private static Map<Metric, Double> initSingleMetricMap(MetricGroup metricGroup) {
		Map<Metric, Double> singleMetricScores = new HashMap<>();

		// init by metrics
		metricGroup.getMetrics().forEach(metric -> singleMetricScores.put(metric, Constant.DEFAULT_SCORE));

		return singleMetricScores;
	}
}
