package metric;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.chain.Context;
import org.apache.commons.chain.Filter;

import model.HealthScore;
import model.HealthScoreContext;
import model.Repo;

/**
 * Average number of commits (push) per day (to any branch) healthRatio =
 * total(PushEvent of project A)/total(PushEvent)
 */
public class HealthScoreAggregator implements Filter {

	@Override
	public boolean execute(Context context) throws Exception {
		List<HealthScore> healthScores = ((HealthScoreContext) context).getHealthScores();

		Map<Long, String> repoNameMap = ((HealthScoreContext) context).getRepoNames();

		
		Map<Long, List<HealthScore>> groupedByRepo = healthScores.stream()
				.collect(Collectors.groupingBy(HealthScore::getRepoId));

		// aggregate heathScore by repo
		healthScores = groupedByRepo.values().stream().map(this::aggregateHealScore)
				.sorted(Comparator.comparing(HealthScore::getScore, Comparator.nullsLast(Comparator.reverseOrder())))
				.collect(Collectors.toList());
		
		// update repo names
		healthScores.forEach(healthScore -> {
			healthScore.setRepoName(repoNameMap.get(healthScore.getRepoId()));
		});
		// update to context
		((HealthScoreContext) context).setHealthScores(healthScores);

		return false;
	}

	@Override
	public boolean postprocess(Context context, Exception exception) {
		
		return false;
	}

	/**
	 * TODO using BigDecimal for more exact
	 * https://stackoverflow.com/questions/5384601/java-doubles-are-not-good-at-math/5385202#5385202
	 * return first object with final aggregate score
	 *
	 * @param healthScores
	 * @return
	 */
	private HealthScore aggregateHealScore(List<HealthScore> healthScores) {
		double finalHealthScore = healthScores.stream().mapToDouble(HealthScore::getScore).reduce(1.0, (a, b) -> a * b);

		healthScores.get(0).setScore(finalHealthScore);

		Double avgCommitScore = getFirstNonNull(HealthScore::getAvgCommitScore, healthScores);
		
		Double avgIssueOpenTimeScore = getFirstNonNull(HealthScore::getAvgIssueOpenTimeScore, healthScores);

		Integer numOfCommit = getFirstNonNull(HealthScore::getNumOfCommit, healthScores);
		
		return HealthScore.builder().score(finalHealthScore)
				.repoId(healthScores.get(0).getRepoId())
				.repoName(healthScores.get(0).getRepoName())
				.avgCommitScore(avgCommitScore == null ? 0.0 : avgCommitScore)
				.avgIssueOpenTimeScore(avgIssueOpenTimeScore == null ? 0.0 : avgIssueOpenTimeScore)
				.numOfCommit(numOfCommit == null? 0 : numOfCommit)
				.build();
	}

	private <T> T getFirstNonNull(Function<HealthScore, T> object, List<HealthScore> healthScores) {
		return healthScores.stream().map(object).filter(Objects::nonNull).findAny()
				.orElse(null);
	}

}
