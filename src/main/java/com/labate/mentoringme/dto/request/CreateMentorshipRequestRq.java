package com.labate.mentoringme.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.labate.mentoringme.dto.model.ShiftDto;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.Set;

@Data
public class CreateMentorshipRequestRq {
  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Long id;

  private Long mentorId;
  private Long categoryId;

  @JsonProperty(access = JsonProperty.Access.READ_ONLY)
  private Long createdBy;

  private String title;
  private Date startDate;
  private Date endDate;
  private Integer duration;
  private Integer type;
  private Long status;
  private Float price;
  private String detailAddress;
  private Long addressId;

  private Set<ShiftDto> shifts;
  // private Set<Integer> studentIds;
}
