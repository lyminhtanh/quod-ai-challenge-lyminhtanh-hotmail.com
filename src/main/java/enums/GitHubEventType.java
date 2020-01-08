package enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum GitHubEventType {
    PUSH_EVENT("PushEvent"),
    ISSUE_EVENT("IssuesEvent");

    private String value;

    private GitHubEventType(String value) {
        this.value = value;
    }

    @JsonValue
    public String value(){
        return this.value;
    }


}
