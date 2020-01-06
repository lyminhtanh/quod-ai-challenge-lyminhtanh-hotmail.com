package model;

import lombok.Data;

@Data
public class Payload {
    private long pushId;

    private int size;

    private Issue issue;

    private String action;

}
