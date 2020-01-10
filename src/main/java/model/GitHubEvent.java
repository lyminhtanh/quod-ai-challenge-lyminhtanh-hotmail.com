package model;

import java.io.IOException;
import java.time.LocalDateTime;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import jackson.CustomDateTimeDeserializer;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Data
public class GitHubEvent {
  private String type;

  private Actor actor;

  private Repo repo;

  private Payload payload;

  @JsonDeserialize(using = CustomDateTimeDeserializer.class)
  private LocalDateTime createdAt;

  public static GitHubEvent fromJson(String json) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);

    GitHubEvent event = null;
    try {
      event = objectMapper.readValue(json, GitHubEvent.class);

    } catch (JsonMappingException | JsonParseException e) {
      log.warn("!!! Json parsing failed. Skip this line.");
    }
    return event;

  }
}
