package model;

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

    private Double score;

    public String[] toCsvRow() {
        return new String[]{
                String.valueOf(repoId),
                repoName,
                String.valueOf(score),
                String.valueOf(avgCommitScore),
                String.valueOf(avgIssueOpenTimeScore),
                String.valueOf(numOfCommit)
        };
    }
}
