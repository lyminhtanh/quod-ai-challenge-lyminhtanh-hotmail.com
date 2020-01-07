package model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jackson.CustomDateTimeDeserializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Issue {

    private long id;

    private String state;

    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    private LocalDateTime createdAt;
}
