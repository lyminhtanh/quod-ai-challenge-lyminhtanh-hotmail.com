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
        long totalPush = 0;
        List<String> lines = new ArrayList<>();
        for (String filePath : FileUtil.listJsonFiles()) {
            lines.addAll(FileUtil.readLinesByEventType(filePath, GitHubEventType.PUSH_EVENT));
            totalPush += lines.size();
        }

        List<HealthScore> healthScores = calculateHealthScore(totalPush, lines);

        List<String[]> csvRows = healthScores.stream()
                .map(this::toCsvRow)
                .collect(Collectors.toList());

        try{
            FileUtil.createCSVFile(csvRows);
            System.out.println(String.format("Exported result to: %s", Constant.OUTPUT_FILE_NAME));
        } catch (IOException ex){

        }
    }

    private String[] toCsvRow(HealthScore healthScore) {
        return new String[]{healthScore.getRepo().getName(), String.valueOf(healthScore.getScore())};
    }

    /**
     * calculate health score for each project
     * @param totalPush
     * @param lines
     * @return  List<HealthScore> odered descending by score
     */
    private List<HealthScore> calculateHealthScore(final long totalPush, List<String> lines) {
        return lines.stream()
                .map(GitHubEvent::fromJson)
                .collect(Collectors.groupingBy(x -> x.getRepo()))
                .entrySet()
                .stream()
                .map(entry -> new HealthScore((Repo)entry.getKey(), (double)entry.getValue().size()/totalPush))
                .sorted(Comparator.comparing(HealthScore::getScore, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

}
