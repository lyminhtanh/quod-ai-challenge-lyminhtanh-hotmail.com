package model;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import enums.GitHubEventType;
import lombok.Data;
import lombok.Getter;

import java.io.IOException;

@Data
public class GitHubEvent {
    private GitHubEventType type;

    private Repo repo;

    private Payload payload;

    public static GitHubEvent fromJson(String json){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setPropertyNamingStrategy(
                PropertyNamingStrategy.SNAKE_CASE);

        GitHubEvent event = null;
        try {
            event = objectMapper.readValue(json, GitHubEvent.class);
        } catch (IOException ex) {
            System.out.println(ex);
        }

        return event;
    }
}
