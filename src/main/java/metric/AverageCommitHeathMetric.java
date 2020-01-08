package metric;

import java.awt.Event;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import enums.GitHubEventType;
import model.GitHubEvent;
import model.HealthScore;
import model.HealthScoreContext;
import model.Repo;
import util.FileUtil;

/**
 * Average number of commits (push) per day (to any branch) healthRatio =
 * total(PushEvent of project A)/total(PushEvent)
 */
public class AverageCommitHeathMetric implements HealthMetric, Command {

	@Override
	public boolean execute(Context context) throws Exception {
		List<HealthScore> healthScores = calculate(((HealthScoreContext) context));
		((HealthScoreContext) context).getHealthScores().addAll(healthScores);
		return false;
	}

	@Override
	public List<HealthScore> calculate(HealthScoreContext context) {
		List<String> lines = new ArrayList<>();

		for (String filePath : FileUtil.listJsonFiles()) {
			lines.addAll(FileUtil.readLinesByEventType(filePath, GitHubEventType.PUSH_EVENT));
		}

		List<GitHubEvent> events = lines.stream().map(GitHubEvent::fromJson).collect(Collectors.toList());

		Map<Long, String> repoNames = events.parallelStream().map(GitHubEvent::getRepo)
				.collect(Collectors.toMap(Repo::getId, Repo::getName, (r1, r2) -> r1 ));

		context.getRepoNames().putAll(repoNames);

		return calculateHealthScore(events);
	}

	/**
	 * calculate health score for each project
	 *
	 * @param events
	 * @return List<HealthScore> odered descending by score
	 */
	private List<HealthScore> calculateHealthScore(List<GitHubEvent> events) {
		// collect number of commits for each repo

		List<HealthScore> healthScores = events.stream().collect(Collectors.groupingBy(x -> x.getRepo().getId()))
				.entrySet().stream()
				.map(entry -> HealthScore.builder().repoId(entry.getKey()).numOfCommit(entry.getValue().size()).build())
				.sorted(Comparator.comparing(HealthScore::getNumOfCommit, Comparator.reverseOrder()))
				.collect(Collectors.toList());

		// select repo has max number of commits
		double maxCommit = healthScores.stream().mapToDouble(HealthScore::getNumOfCommit).max().getAsDouble();

		// update score
		healthScores.forEach(healthScore -> {
			healthScore.setScore(healthScore.getNumOfCommit() / maxCommit);
			healthScore.setAvgCommitScore(healthScore.getScore());
		});

		return healthScores;
	}

}
