package util;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.chain.Command;

import enums.Metric;
import metric.MetricCatalog;
import model.HealthScore;
import model.HealthScoreContext;

public class ChainUtil {

	/**
	 * execute Chain by context
	 * @param context
	 */
	public static void executeChain(HealthScoreContext context) {
		try {
			Command allMetricChain = new MetricCatalog().getCommand(context.getMetricGroup().name());
			allMetricChain.execute(context);
		} catch (Exception e) {

		}

	}

	/**
	 * merge results in to context's healthScores
	 * @param ctxHealthScores
	 * @param currentMetricHealthScores
	 */
	public static void mergeHealthScores(List<HealthScore> ctxHealthScores, List<HealthScore> currentMetricHealthScores) {

		Set<Long> ctxRepoIds = ctxHealthScores.stream().map(HealthScore::getRepoId).collect(Collectors.toSet());
		
		for(HealthScore healthScore : currentMetricHealthScores) {
			Long repoId = healthScore.getRepoId();
			
			// multiple score if repo exists
			if(ctxRepoIds.contains(repoId)){
				ctxHealthScores
				.stream()
				.filter(ctxHs -> ctxHs.getRepoId().equals(repoId))
				.findAny()
				//TODO update to map single
				.ifPresent(ctxHs -> ctxHs.setScore(ctxHs.getScore()*healthScore.getScore()));
			
			} else { // add new repo
				ctxHealthScores.add(healthScore);
			}
		}
	}
}
