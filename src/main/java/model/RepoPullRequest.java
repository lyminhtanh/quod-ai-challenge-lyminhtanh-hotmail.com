package model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RepoPullRequest {

  private long repoId;

  private long pullRequestId;
}
