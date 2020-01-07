package metric;

import constant.Constant;
import enums.GitHubEventType;
import model.GitHubEvent;
import model.HealthScore;
import model.Repo;
import util.FileUtil;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Average number of commits (push) per day (to any branch)
 * healthRatio = total(PushEvent of project A)/total(PushEvent)
 */
public class AverageCommitHeathMetric implements HealthMetric {

    @Override
    public void calculate() {
        List<String> lines = new ArrayList<>();

        for (String filePath : FileUtil.listJsonFiles()) {
            lines.addAll(FileUtil.readLinesByEventType(filePath, GitHubEventType.PUSH_EVENT));
        }

        List<HealthScore> healthScores = calculateHealthScore(lines);

        List<String[]> csvRows = healthScores.stream()
                .map(HealthScore::toCsvRow)
                .collect(Collectors.toList());

        try {
            FileUtil.createCSVFile(csvRows);
            System.out.println(String.format("Exported result to: %s", Constant.OUTPUT_FILE_NAME));
        } catch (IOException ex){

        }
    }

    /**
     * calculate health score for each project
     * @param lines
     * @return  List<HealthScore> odered descending by score
     */
    private List<HealthScore> calculateHealthScore(List<String> lines) {
        // collect number of commits for each repo
        List<HealthScore> healthScores = lines.stream()
                .map(GitHubEvent::fromJson)
                .collect(Collectors.groupingBy(x -> x.getRepo()))
                .entrySet()
                .stream()
                .map(entry -> HealthScore.builder()
                        .repo((Repo)entry.getKey())
                        .numOfCommit(entry.getValue().size())
                        .build())
                .sorted(Comparator.comparing(HealthScore::getNumOfCommit, Comparator.reverseOrder()))
                .collect(Collectors.toList());

        // select repo has max number of commits
        double maxCommit = healthScores.stream().mapToDouble(HealthScore::getNumOfCommit).max().getAsDouble();

        // update score
        healthScores.forEach(healthScore -> healthScore.setScore(healthScore.getNumOfCommit()/maxCommit));

        return healthScores;
    }

}
