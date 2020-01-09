package enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum GitHubEventType {
  PUSH_EVENT("PushEvent"), ISSUE_EVENT("IssuesEvent"), PULL_REQUEST_EVENT(
      "PullRequestEvent"), RELEASE_EVENT("ReleaseEvent");

  private String value;

  private GitHubEventType(String value) {
    this.value = value;
  }

  @JsonValue
  public String value() {
    return this.value;
  }

}
