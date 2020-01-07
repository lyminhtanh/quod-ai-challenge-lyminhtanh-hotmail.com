package metric;

import constant.Constant;
import enums.Action;
import enums.GitHubEventType;
import lombok.Builder;
import lombok.Data;
import model.*;
import util.FileUtil;
import util.NormalizeUtil;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Average time that an issue remains opened
 * healthRatio = total(PushEvent of project A)/total(PushEvent)
 */
@Data
@Builder
public class AverageIssueOpenedTimeHeathMetric implements HealthMetric {

    private LocalDateTime dateTimeStart;

    private LocalDateTime dateTimeEnd;


    @Override
    public void calculate() {
        List<String> lines = new ArrayList<>();
        for (String filePath : FileUtil.listJsonFiles()) {
            lines.addAll(FileUtil.readLinesByEventType(filePath, GitHubEventType.ISSUE_EVENT));
        }


        Map<RepoIssue, List<GitHubEvent>> groupedByRepoIssue = lines.stream()
                .map(GitHubEvent::fromJson)
                .collect(Collectors.groupingBy(this::buildRepoIssueKey));

        groupedByRepoIssue.values()
                .stream()
                .filter(events -> events.size() > 1)
                .forEach(events -> events.sort(Comparator.comparing(GitHubEvent::getCreatedAt, Comparator.naturalOrder())));

        Map<RepoIssue, Long> timeGroupedByRepoIssue = groupedByRepoIssue.entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> entry.getKey(), this::calculateOpenTimeInMinutes));

        List<HealthScore> healthScores = timeGroupedByRepoIssue.entrySet()
                .stream()
                .collect(Collectors.groupingBy(entry -> entry.getKey().getRepoId()))
                .entrySet()
                .stream()
                .map(entry -> calculateHealthScore(entry))
                .sorted(Comparator.comparing(HealthScore::getScore, Comparator.reverseOrder()))
                .collect(Collectors.toList());

        NormalizeUtil.normalize(healthScores);

        List<String[]> csvRows = healthScores.stream()
                .map(this::toCsvRow)
                .collect(Collectors.toList());

        try {
            FileUtil.createCSVFile(csvRows);
            System.out.println(String.format("Exported result to: %s", Constant.OUTPUT_FILE_NAME));
        } catch (IOException ex) {

        }
    }

    /**
     * @param entry
     * @return
     */
    private HealthScore calculateHealthScore(Map.Entry<Long, List<Map.Entry<RepoIssue, Long>>> entry) {
        double score = calculateHealthScore(entry.getValue());

        // TODO add repo name
        Repo repo = Repo.builder().id(entry.getKey()).name(Constant.EMPTY_STRING).build();
        return HealthScore.builder().repo(repo).score(score).build();
    }


    /**
     * @param entry
     * @return
     */
    private Long calculateOpenTimeInMinutes(Map.Entry<RepoIssue, List<GitHubEvent>> entry) {
        final LocalDateTime openedAt = Optional.ofNullable(entry.getValue().get(0))
                .map(GitHubEvent::getPayload)
                .map(Payload::getIssue)
                .map(Issue::getCreatedAt)
                .orElseThrow(() -> new IllegalStateException(String.format("No created_at found for Issue: %d", entry.getKey().getIssueId())));

        final LocalDateTime nonOpenedAt = getNonOpenActionCreatedAt(entry.getValue());
        Duration duration = Duration.between(openedAt, nonOpenedAt);

        return duration.toMinutes();
    }

    private LocalDateTime getNonOpenActionCreatedAt(List<GitHubEvent> events) {
        boolean hasOpenAction = hasOpenedAction(events);

        int nonOpenedIndex = 0;

        if(hasOpenAction){
            if(events.size() == 1){
                return this.dateTimeEnd.plusHours(1);
            }
            nonOpenedIndex = 1;
        }

        return events.get(nonOpenedIndex).getCreatedAt();

    }
    
    private boolean hasOpenedAction(List<GitHubEvent> events) {
        return events.stream()
                .map(GitHubEvent::getPayload)
                .map(Payload::getAction)
                .anyMatch(Action.OPENED.value()::equals);
    }

    private RepoIssue buildRepoIssueKey(GitHubEvent event) {
        return new RepoIssue(event.getRepo().getId(), event.getPayload().getIssue().getId());
    }

    private String[] toCsvRow(HealthScore healthScore) {
        return new String[]{healthScore.getRepo().getName(), String.valueOf(healthScore.getScore())};
    }

    /**
     * calculate health score for each project
     *
     * @return List<HealthScore> odered descending by score
     */
    private double calculateHealthScore(List<Map.Entry<RepoIssue, Long>> entries) {
        if(entries.size() == 0){
            return 0;
        }
        Long avgOpenTime = entries.stream().map(Map.Entry::getValue).mapToLong(Long::longValue).sum() / entries.size();
        Long minOpenTime = entries.stream().map(Map.Entry::getValue).mapToLong(Long::longValue).min().getAsLong();
        if(minOpenTime == 0){
            return 0;
        }
        System.out.println(avgOpenTime + "____" + minOpenTime);
        return (double) avgOpenTime / minOpenTime;
    }

}