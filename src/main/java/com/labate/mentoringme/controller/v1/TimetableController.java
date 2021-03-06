package com.labate.mentoringme.controller.v1;

import com.labate.mentoringme.config.CurrentUser;
import com.labate.mentoringme.dto.mapper.TimetableMapper;
import com.labate.mentoringme.dto.model.LocalUser;
import com.labate.mentoringme.dto.request.CreateEventRequest;
import com.labate.mentoringme.dto.request.DeleteEventRequest;
import com.labate.mentoringme.dto.request.GetTimetableRequest;
import com.labate.mentoringme.dto.response.BaseResponseEntity;
import com.labate.mentoringme.exception.TimetableNotFoundException;
import com.labate.mentoringme.service.timetable.TimetableService;
import io.swagger.annotations.ApiImplicitParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/timetables")
public class TimetableController {
  private final TimetableService timetableService;

  @GetMapping("/users/{userId}")
  public ResponseEntity<?> findTimetableById(
      @PathVariable Long userId, @Valid GetTimetableRequest request) {
    var timetable = timetableService.findByUserId(userId, request);
    if (timetable == null) {
      throw new TimetableNotFoundException("userId = " + userId);
    }
    return BaseResponseEntity.ok(TimetableMapper.toDto(timetable));
  }

  @ApiImplicitParam(
      name = "Authorization",
      value = "Access Token",
      required = true,
      paramType = "header",
      dataTypeClass = String.class,
      example = "Bearer access_token")
  @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'MENTOR', 'USER')")
  @GetMapping("/me")
  public ResponseEntity<?> getMyTimetable(
      @CurrentUser LocalUser localUser, @Valid GetTimetableRequest request) {
    Long userId = localUser.getUserId();
    return findTimetableById(userId, request);
  }

  // @ApiImplicitParam(
  //     name = "Authorization",
  //     value = "Access Token",
  //     required = true,
  //     paramType = "header",
  //     dataTypeClass = String.class,
  //     example = "Bearer access_token")
  // @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
  // @PutMapping("/{timetableId}")
  // public ResponseEntity<?> updateTimetable(
  //     @PathVariable Long timetableId, @Valid @RequestBody TimetableDto timetableDto) {
  //
  //   var oldTimetable = timetableService.findById(timetableId);
  //   if (oldTimetable == null) {
  //     throw new TimetableNotFoundException("id = " + timetableId);
  //   }
  //
  //   timetableDto.setId(timetableId);
  //   var timetable = TimetableMapper.toEntity(timetableDto);
  //   timetable = timetableService.saveTimetable(timetable);
  //
  //   return BaseResponseEntity.ok(TimetableMapper.toDto(timetable));
  // }

  @ApiImplicitParam(
      name = "Authorization",
      value = "Access Token",
      required = true,
      paramType = "header",
      dataTypeClass = String.class,
      example = "Bearer access_token")
  @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'MENTOR', 'USER')")
  @PostMapping("/events")
  public ResponseEntity<?> createNewEvents(
      @Valid @RequestBody CreateEventRequest request, @CurrentUser LocalUser localUser) {

    timetableService.createNewEvents(request, localUser);

    return BaseResponseEntity.ok(null, "Events created successfully");
  }

  @ApiImplicitParam(
          name = "Authorization",
          value = "Access Token",
          required = true,
          paramType = "header",
          dataTypeClass = String.class,
          example = "Bearer access_token")
  @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'MENTOR', 'USER')")
  @DeleteMapping("/events")
  public ResponseEntity<?> deleteEvents(
          @Valid @RequestBody DeleteEventRequest request, @CurrentUser LocalUser localUser) {

    timetableService.deleteEvents(request, localUser);

    return BaseResponseEntity.ok(null, "Events deleted successfully");
  }
}
