package com.labate.mentoringme.dto;

import lombok.Data;

@Data
public class QuizOverviewDto {
  private Long id;
  private String title;
  private Integer numberOfQuestion;
  private Integer time;
  private Boolean isDraft;
}
