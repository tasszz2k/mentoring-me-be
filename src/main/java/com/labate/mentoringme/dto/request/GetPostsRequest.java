package com.labate.mentoringme.dto.request;

import com.labate.mentoringme.model.Post;
import lombok.Builder;
import lombok.Value;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Builder
@Value
public class GetPostsRequest {
  String keyword;

  Long createdBy;
  List<Long> categoryIds;

  Post.Status status;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  Date fromDate;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  Date toDate;

  Float minPrice;
  Float maxPrice;
}
