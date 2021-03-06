package com.labate.mentoringme.dto.request.quiz;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import com.labate.mentoringme.dto.model.CategoryDto;
import com.labate.mentoringme.dto.model.QuestionDto;
import lombok.Data;

@Data
public class CreateQuizRequest {
  @NotBlank
  private String title;
  @NotNull
  private Integer numberOfQuestion;
  @NotNull
  private Integer time;
  @NotNull
  private Boolean isDraft;
  @NotNull
  private List<CategoryDto> categories;
  private List<QuestionDto> questions;
}
