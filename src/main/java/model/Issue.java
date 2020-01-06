package model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Issue {

    private long id;

    private String state;

    private LocalDateTime createdAt;
}
