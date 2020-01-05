package enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum GitHubEventType {
    PUSH_EVENT("PushEvent");

    private String value;

    private GitHubEventType(String value) {
        this.value = value;
    }

    @JsonValue
    public String value(){
        return this.value;
    }
}
