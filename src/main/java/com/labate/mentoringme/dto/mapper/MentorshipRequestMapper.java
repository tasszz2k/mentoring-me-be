package com.labate.mentoringme.dto.mapper;

import com.labate.mentoringme.dto.model.MentorshipRequestDto;
import com.labate.mentoringme.dto.request.CreateMentorshipRequestRq;
import com.labate.mentoringme.model.MentorshipRequest;
import com.labate.mentoringme.repository.RoleRepository;
import com.labate.mentoringme.service.user.UserService;
import com.labate.mentoringme.util.ObjectMapperUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MentorshipRequestMapper {

  private static UserService userService;
  private static RoleRepository roleRepository;

  @Autowired
  public MentorshipRequestMapper(UserService userService, RoleRepository roleRepository) {
    MentorshipRequestMapper.userService = userService;
    MentorshipRequestMapper.roleRepository = roleRepository;
  }

  public static MentorshipRequestDto toDto(MentorshipRequest entity) {
    if (entity == null) {
      return null;
    }

    var dto = ObjectMapperUtils.map(entity, MentorshipRequestDto.class);
    if (entity.getMentorship() != null) {
      var mentorShipDto = MentorshipMapper.toDto(entity.getMentorship());
      dto.setMentorship(mentorShipDto);
    }

    if (entity.getApproverId() != null) {
      var basicUserInfo = userService.findBasicUserInfoByUserId(entity.getApproverId());
      if (basicUserInfo != null) {
        dto.setAssignee(basicUserInfo);
      }
    }

    if (entity.getApproverId() != null) {
      var basicUserInfo = userService.findBasicUserInfoByUserId(entity.getApproverId());
      if (basicUserInfo != null) {
        dto.setApprover(basicUserInfo);
      }
    }
    dto.setAssigneeRole(entity.getAssigneeRole().getUserRole());

    return dto;
  }

  public static MentorshipRequest toEntity(MentorshipRequestDto dto) {
    if (dto == null) {
      return null;
    }

    return ObjectMapperUtils.map(dto, MentorshipRequest.class);
  }

  public static MentorshipRequest toEntity(CreateMentorshipRequestRq dto) {
    if (dto == null) {
      return null;
    }
    var entity = ObjectMapperUtils.map(dto, MentorshipRequest.class);
    var mentorship = MentorshipMapper.toEntity(dto.getMentorship());
    entity.setMentorship(mentorship);
    entity.setAssigneeRole(roleRepository.findByName(dto.getAssigneeRole().name()));

    return entity;
  }

  public static List<MentorshipRequestDto> toDtos(Collection<MentorshipRequest> entities) {
    return entities == null
        ? null
        : entities.stream().map(MentorshipRequestMapper::toDto).collect(Collectors.toList());
  }

  public static List<MentorshipRequest> toEntities(Collection<MentorshipRequestDto> dtos) {
    return dtos == null
        ? null
        : dtos.stream().map(MentorshipRequestMapper::toEntity).collect(Collectors.toList());
  }
}
