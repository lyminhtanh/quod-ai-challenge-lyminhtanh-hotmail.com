package metric;

import constant.Constant;
import enums.GitHubEventType;
import model.GitHubEvent;
import model.HealthScore;
import model.Repo;
import model.RepoIssue;
import util.FileUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Average time that an issue remains opened
 * healthRatio = total(PushEvent of project A)/total(PushEvent)
 */
public class AverageIssueOpenedTimeHeathMetric implements HealthMetric {

    @Override
    public void calculate() {
        long totalPush = 0;
        List<String> lines = new ArrayList<>();
        for (String filePath : FileUtil.listJsonFiles()) {
            lines.addAll(FileUtil.readLinesByEventType(filePath, GitHubEventType.ISSUE_EVENT));
            totalPush += lines.size();
        }


        Map<RepoIssue, List<GitHubEvent>> groupedByRepoIssue = lines.stream()
                .map(GitHubEvent::fromJson)
                .collect(Collectors.groupingBy(this::buildRepoIssueKey));

        groupedByRepoIssue.values()
                .stream()
                .filter(events -> events.size() > 1)
                .forEach(events -> events.sort(Comparator.comparing(GitHubEvent::getCreatedAt, Comparator.naturalOrder())));

        Map<RepoIssue, Integer> timeGroupedByRepoIssue = groupedByRepoIssue.entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> entry.getKey(), this::calculateOpenTimeInHours));

        List<HealthScore> healthScores = timeGroupedByRepoIssue.entrySet()
                .stream()
                .collect(Collectors.groupingBy(entry -> entry.getKey().getRepoId()))
                .entrySet()
                .stream()
                .map(entry -> calculateHealthScore(entry))
                .collect(Collectors.toList());


        List<String[]> csvRows = healthScores.stream()
                .map(this::toCsvRow)
                .collect(Collectors.toList());

        try{
            FileUtil.createCSVFile(csvRows);
            System.out.println(String.format("Exported result to: %s", Constant.OUTPUT_FILE_NAME));
        } catch (IOException ex){

        }
    }

    /**
     *
     * @param entry
     * @return
     */
    private HealthScore calculateHealthScore(Map.Entry<Object, List<Map.Entry<RepoIssue, Integer>>> entry) {
        double score = calculateHealthScore(entry.getValue());
        return new HealthScore(new Repo(((Long)entry.getKey()).longValue(), "TODO repo name"), score);
    }

    /**
     *
     * @param entry
     * @return
     */
    private Integer calculateOpenTimeInHours(Map.Entry<RepoIssue, List<GitHubEvent>> entry) {
        final int openAt = entry.getValue().get(0).getCreatedAt().getHour();
        final int finishOpenAt = entry.getValue().get(1).getCreatedAt().getHour();
        final int openTime = finishOpenAt - openAt;
        return openTime >= 0 ? openTime : 0;
    }

    private RepoIssue buildRepoIssueKey(GitHubEvent event) {
        return RepoIssue(event.getRepo().getId(), event.getPayload().getIssue().getId());
    }

    private String[] toCsvRow(HealthScore healthScore) {
        return new String[]{healthScore.getRepo().getName(), String.valueOf(healthScore.getScore())};
    }

    /**
     * calculate health score for each project
     * @return  List<HealthScore> odered descending by score
     */
    private double calculateHealthScore(List<Map.Entry<RepoIssue, Integer>> entries) {
        Integer avgOpenTime = entries.stream().map(Map.Entry::getValue).mapToInt(Integer::intValue).sum()/entries.size();
        Integer minOpenTime = entries.stream().map(Map.Entry::getValue).mapToInt(Integer::intValue).min().getAsInt();
        return (double)avgOpenTime/minOpenTime;
    }

}
