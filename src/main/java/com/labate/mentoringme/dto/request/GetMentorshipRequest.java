package com.labate.mentoringme.dto.request;

import com.labate.mentoringme.model.Mentorship;
import lombok.Builder;
import lombok.Value;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Builder
@Value
public class GetMentorshipRequest {
  Long createdBy;
  Long mentorId;
  List<Long> categoryIds;
  List<Long> addressIds;

  Mentorship.Status status;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  Date fromDate;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  Date toDate;

  Float minPrice;
  Float maxPrice;
}