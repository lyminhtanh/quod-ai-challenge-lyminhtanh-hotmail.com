package model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RepoIssue {

    private long repoId;

    private long issueId;
}
