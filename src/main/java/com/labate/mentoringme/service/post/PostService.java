package com.labate.mentoringme.service.post;

import com.labate.mentoringme.dto.model.LocalUser;
import com.labate.mentoringme.dto.model.PostDto;
import com.labate.mentoringme.dto.request.CreatePostRequest;
import com.labate.mentoringme.dto.request.GetPostsRequest;
import com.labate.mentoringme.dto.request.PageCriteria;
import com.labate.mentoringme.dto.response.PageResponse;
import com.labate.mentoringme.model.Post;
import com.labate.mentoringme.model.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PostService {
  Post savePost(Post post);

  Post findPostById(Long id);

  Page<Post> findAllPosts(PageCriteria pageCriteria, GetPostsRequest request);

  Post updatePost(CreatePostRequest request, LocalUser localUser);

  void deletePost(Long id);

  void likePost(Long postId, Long userId);

  void unlikePost(Long postId, Long userId);

  Post updateStatus(Long postId, Post.Status status, LocalUser localUser);

  List<User> getAllUserLikePost(Long id);

  void updateCommentCount(Long postId, int number);

  PostDto findPostDtoById(Long postId, LocalUser localUser);

  PageResponse findAllPostDtosByConditions(PageCriteria pageCriteria, GetPostsRequest request, LocalUser localUser);
}
