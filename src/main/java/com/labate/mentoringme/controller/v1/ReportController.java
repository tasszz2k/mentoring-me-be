package com.labate.mentoringme.controller.v1;

import java.util.List;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.labate.mentoringme.config.CurrentUser;
import com.labate.mentoringme.dto.model.LocalUser;
import com.labate.mentoringme.dto.request.CreateReportRequest;
import com.labate.mentoringme.dto.request.PageCriteria;
import com.labate.mentoringme.dto.response.BaseResponseEntity;
import com.labate.mentoringme.dto.response.PageResponse;
import com.labate.mentoringme.dto.response.Paging;
import com.labate.mentoringme.exception.ReportNotFoundException;
import com.labate.mentoringme.service.report.ReportService;
import io.swagger.annotations.ApiImplicitParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

  private final ReportService reportService;

  @GetMapping()
  public ResponseEntity<?> getAllReport(@Valid PageCriteria pageCriteria) {
    pageCriteria.setSort(List.of("-createdDate"));
    var pageData = reportService.getAllReport(pageCriteria);
    var paging = Paging.builder().limit(pageCriteria.getLimit()).page(pageCriteria.getPage())
        .total(pageData.getTotalElements()).build();
    var pageResponse = new PageResponse(pageData.getContent(), paging);
    return BaseResponseEntity.ok(pageResponse);
  }

  @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true,
      paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
  @PostMapping()
  @PreAuthorize("hasAnyRole('USER')")
  public ResponseEntity<?> addReport(CreateReportRequest createReportRequest,
      @CurrentUser LocalUser localUser) throws Exception {
    var report = reportService.createReport(createReportRequest, localUser);
    return BaseResponseEntity.ok(report);
  }

  @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true,
      paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
  public ResponseEntity<?> getDetailReport(@PathVariable Long id) {
    var report = reportService.getDetailReport(id);
    if (report.isEmpty()) {
      throw new ReportNotFoundException("report not found id: " + id);
    }
    return BaseResponseEntity.ok(report.get());
  }

  @ApiImplicitParam(name = "Authorization", value = "Access Token", required = true,
      paramType = "header", dataTypeClass = String.class, example = "Bearer access_token")
  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
  public ResponseEntity<?> deleteReport(@PathVariable Long id) {
    reportService.deleteReportById(id);
    return BaseResponseEntity.ok("delete report successfully");
  }
}
