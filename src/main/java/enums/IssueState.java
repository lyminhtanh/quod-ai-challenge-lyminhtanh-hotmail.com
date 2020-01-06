package enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum IssueState {
    OPEN("open");

    private String value;

    private IssueState(String value) {
        this.value = value;
    }

    @JsonValue
    public String value(){
        return this.value;
    }
}
