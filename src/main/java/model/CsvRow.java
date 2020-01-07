package model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CsvRow {

    private  String repoName;

    private double healthScore;

    private int numOfCommit;

    public String[] toCsvRow() {
        return new String[]{
                repoName,
                String.valueOf(healthScore),
                String.valueOf(numOfCommit)
        };
    }
}
