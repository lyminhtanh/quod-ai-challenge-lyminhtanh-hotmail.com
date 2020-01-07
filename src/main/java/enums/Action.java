package enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Action {
    OPENED("opened");

    private String value;

    private Action(String value) {
        this.value = value;
    }

    @JsonValue
    public String value(){
        return this.value;
    }
}
