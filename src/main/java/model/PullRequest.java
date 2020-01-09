package model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import jackson.CustomDateTimeDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PullRequest {

  private Long id;

  @JsonDeserialize(using = CustomDateTimeDeserializer.class)
  private LocalDateTime createdAt;

  @JsonDeserialize(using = CustomDateTimeDeserializer.class)
  private LocalDateTime mergedAt;

}
