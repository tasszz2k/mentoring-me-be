package com.labate.mentoringme.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Getter
@Setter
public class QuizTakingHistoryResponse {
  private String title;

  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd HH:mm:ss",
      timezone = "Asia/Bangkok")
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date created;

  private Integer score;
  private Integer numberOfTrue;
  private Integer numberOfFalse;
  private Integer numberOfQuestion;
  private String author;
}
