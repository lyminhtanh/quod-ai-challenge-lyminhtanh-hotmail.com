package model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthScore {
    private Repo repo = new Repo();

    private int numOfCommit;

    private double score;

    public String[] toCsvRow() {
        return new String[]{
                repo.getName(),
                String.valueOf(score),
                String.valueOf(numOfCommit)
        };
    }
}
