package com.labate.mentoringme.dto.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuizResultCheckingDto {
  private Long questionId;
  private Long answerId;
}
