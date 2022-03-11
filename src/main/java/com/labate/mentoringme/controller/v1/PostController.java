package com.labate.mentoringme.controller.v1;

import com.labate.mentoringme.config.CurrentUser;
import com.labate.mentoringme.dto.mapper.PostMapper;
import com.labate.mentoringme.dto.model.LocalUser;
import com.labate.mentoringme.dto.request.CreatePostRequest;
import com.labate.mentoringme.dto.request.GetPostsRequest;
import com.labate.mentoringme.dto.request.PageCriteria;
import com.labate.mentoringme.dto.response.BaseResponseEntity;
import com.labate.mentoringme.dto.response.PageResponse;
import com.labate.mentoringme.dto.response.Paging;
import com.labate.mentoringme.exception.PostNotFoundException;
import com.labate.mentoringme.service.post.PostService;
import io.swagger.annotations.ApiImplicitParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/posts")
public class PostController {
  private final PostService postService;

  @GetMapping("/{id}")
  public ResponseEntity<?> findPostById(@PathVariable Long id) {
    var post = postService.findPostById(id);
    if (post == null) {
      throw new PostNotFoundException("id = " + id);
    }
    return BaseResponseEntity.ok(PostMapper.toDto(post));
  }

  @GetMapping("")
  public ResponseEntity<?> findAllPostsByConditions(
      @Valid PageCriteria pageCriteria, @Valid GetPostsRequest request) {
    var page = postService.findAllPosts(pageCriteria, request);
    var classes = page.getContent();

    var paging =
        Paging.builder()
            .limit(pageCriteria.getLimit())
            .page(pageCriteria.getPage())
            .total(page.getTotalElements())
            .build();
    var response = new PageResponse(PostMapper.toDtos(classes), paging);
    return BaseResponseEntity.ok(response);
  }

  @GetMapping("/top-posts")
  public ResponseEntity<?> findTop10Posts() {

    var sort = List.of("-createdDate");
    PageCriteria pageCriteria = PageCriteria.builder().limit(10).page(1).sort(sort).build();
    GetPostsRequest request = GetPostsRequest.builder().build();
    return findAllPostsByConditions(pageCriteria, request);
  }

  @ApiImplicitParam(
      name = "Authorization",
      value = "Access Token",
      required = true,
      paramType = "header",
      dataTypeClass = String.class,
      example = "Bearer access_token")
  @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'MENTOR', 'USER')")
  @PostMapping("")
  public ResponseEntity<?> addNewPost(
      @Valid @RequestBody CreatePostRequest request, @CurrentUser LocalUser localUser) {
    request.setId(null);
    var entity = PostMapper.toEntity(request, localUser);
    var savedEntity = postService.savePost(entity);

    return BaseResponseEntity.ok(
        PostMapper.toDto(savedEntity), "Post has been created successfully");
  }

  @ApiImplicitParam(
      name = "Authorization",
      value = "Access Token",
      required = true,
      paramType = "header",
      dataTypeClass = String.class,
      example = "Bearer access_token")
  @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'MENTOR', 'USER')")
  @PutMapping("/{id}")
  public ResponseEntity<?> updatePost(
      @PathVariable Long id,
      @Valid @RequestBody CreatePostRequest request,
      @CurrentUser LocalUser localUser) {
    request.setId(id);
    postService.updatePost(request, localUser);
    return BaseResponseEntity.ok(null, "Post updated successfully");
  }

  @ApiImplicitParam(
      name = "Authorization",
      value = "Access Token",
      required = true,
      paramType = "header",
      dataTypeClass = String.class,
      example = "Bearer access_token")
  @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR', 'MENTOR', 'USER')")
  @PatchMapping("/{id}/status")
  public ResponseEntity<?> updatePostStatus(
      @PathVariable Long id,
      @Valid @RequestBody CreatePostRequest request,
      @CurrentUser LocalUser localUser) {
    postService.updateStatus(id, request.getStatus(), localUser);
    return BaseResponseEntity.ok(null, "Post status updated successfully");
  }
}