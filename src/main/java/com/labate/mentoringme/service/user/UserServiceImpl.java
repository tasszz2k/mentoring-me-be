package com.labate.mentoringme.service.user;

import com.labate.mentoringme.constant.AvatarConstant;
import com.labate.mentoringme.constant.MentorStatus;
import com.labate.mentoringme.constant.SocialProvider;
import com.labate.mentoringme.constant.UserRole;
import com.labate.mentoringme.dto.mapper.PageCriteriaPageableMapper;
import com.labate.mentoringme.dto.mapper.UserMapper;
import com.labate.mentoringme.dto.model.BasicUserInfo;
import com.labate.mentoringme.dto.model.LocalUser;
import com.labate.mentoringme.dto.model.UserDetails;
import com.labate.mentoringme.dto.projection.BasicUserInfoProjection;
import com.labate.mentoringme.dto.request.FindUsersRequest;
import com.labate.mentoringme.dto.request.PageCriteria;
import com.labate.mentoringme.dto.request.SignUpRequest;
import com.labate.mentoringme.dto.request.UpdateCometChatRequest;
import com.labate.mentoringme.exception.OAuth2AuthenticationProcessingException;
import com.labate.mentoringme.exception.UserAlreadyExistAuthenticationException;
import com.labate.mentoringme.exception.UserNotFoundException;
import com.labate.mentoringme.model.FavoriteMentor;
import com.labate.mentoringme.model.Role;
import com.labate.mentoringme.model.User;
import com.labate.mentoringme.model.UserProfile;
import com.labate.mentoringme.repository.FavoriteMentorRepository;
import com.labate.mentoringme.repository.RoleRepository;
import com.labate.mentoringme.repository.UserRepository;
import com.labate.mentoringme.security.oauth2.user.OAuth2UserInfo;
import com.labate.mentoringme.security.oauth2.user.OAuth2UserInfoFactory;
import com.labate.mentoringme.service.cometchat.ComeTChatService;
import com.labate.mentoringme.service.gcp.GoogleCloudFileUpload;
import com.labate.mentoringme.service.timetable.TimetableService;
import com.labate.mentoringme.service.userprofile.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Log4j2
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserProfileService userProfileService;
  private final TimetableService timetableService;
  private final GoogleCloudFileUpload googleCloudFileUpload;
  private final MentorVerificationService mentorVerificationService;
  private final UserCache userCache;
  private final FavoriteMentorRepository favoriteMentorRepository;
  private final ComeTChatService comeTChatService;

  @Value("${labate.security.default-password}")
  private String defaultPassword;

  @Override
  @Transactional(value = "transactionManager")
  public User registerNewUser(final SignUpRequest signUpRequest)
      throws UserAlreadyExistAuthenticationException {
    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      throw new UserAlreadyExistAuthenticationException("email = " + signUpRequest.getEmail());
    }
    User user = buildUser(signUpRequest);
    if (UserRole.ROLE_MENTOR.equals(signUpRequest.getRole())) {
      user.setStatus(MentorStatus.IN_PROGRESS);
    }

    user = save(user);
    Long userId = user.getId();
    timetableService.createNewTimetable(
        userId, String.format("Th???i kh??a bi???u c???a %s", user.getFullName()));

    if (UserRole.ROLE_MENTOR.equals(signUpRequest.getRole())) {
      mentorVerificationService.registerMentor(userId, null);
    }
    userRepository.flush();
    return user;
  }

  @Override
  public boolean existsByEmail(String email) {
    return userRepository.existsByEmail(email);
  }

  private User buildUser(final SignUpRequest formDto) {
    var user = new User();
    user.setFullName(formDto.getFullName());
    user.setEmail(formDto.getEmail());
    user.setPassword(passwordEncoder.encode(formDto.getPassword()));

    // user roles
    final var roles = new HashSet<Role>();
    var role = formDto.getRole();
    roles.add(roleRepository.findByName(role.name()));
    user.setRoles(roles);
    user.setProvider(formDto.getSocialProvider().getProviderType());
    user.setEnabled(true);
    user.setProviderUserId(formDto.getProviderUserId());

    // user profile
    var userProfile = new UserProfile();
    userProfile.setIsDeleted(false);
    user.setUserProfile(userProfile);

    return user;
  }

  @Override
  public User findUserByEmail(final String email) {
    // Stupid way :)))
    var userId = userCache.findUserIdByEmail(email);
    if (userId == null) {
      return null;
    }
    return userCache.findUserById(userId).orElse(null);
  }

  @Override
  @Transactional
  public LocalUser processUserRegistration(
      String registrationId,
      Map<String, Object> attributes,
      OidcIdToken idToken,
      OidcUserInfo userInfo) {
    OAuth2UserInfo oAuth2UserInfo =
        OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);
    if (!StringUtils.hasText(oAuth2UserInfo.getName())) {
      throw new OAuth2AuthenticationProcessingException("Name not found from OAuth2 provider");
    } else if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
      throw new OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider");
    }
    SignUpRequest userDetails = toUserRegistrationObject(registrationId, oAuth2UserInfo);
    User user = findUserByEmail(oAuth2UserInfo.getEmail());
    if (user != null) {
      if (!user.getProvider().equals(registrationId)
          && !user.getProvider().equals(SocialProvider.LOCAL.getProviderType())) {
        throw new OAuth2AuthenticationProcessingException(
            "Looks like you're signed up with "
                + user.getProvider()
                + " account. Please use your "
                + user.getProvider()
                + " account to login.");
      }
      user = updateExistingUser(user, oAuth2UserInfo);
    } else {
      user = registerNewUser(userDetails);
    }

    return LocalUser.create(user, attributes, idToken, userInfo);
  }

  @CachePut(value = "user", key = "{#user.id}")
  @Transactional
  @Override
  public User save(User user) {
    var isSyncNewChatUser = user.getId() == null;
    var imageUrl1 = user.getImageUrl();
    if (imageUrl1 == null || AvatarConstant.DEFAULT_AVATARS.contains(imageUrl1)) {
      var imageUrl = AvatarConstant.getUrl(user.getRole(), user.getUserProfile().getGender());
      user.setImageUrl(imageUrl);
    }
    var updatedUser = userRepository.save(user);
    userProfileService.save(user.getUserProfile());
    if (isSyncNewChatUser) {
      comeTChatService.addUserToDashboard(user);
    } else {
      var updateCometChatRequest =
          UpdateCometChatRequest.builder()
              .userId(String.valueOf(user.getId()))
              .name(updatedUser.getFullName())
              .imgUrl(updatedUser.getImageUrl())
              .build();
      comeTChatService.updateUser(updateCometChatRequest);
    }
    return updatedUser;
  }

  @Override
  public void updateUserEnableStatus(Long userId, boolean enable) throws UserNotFoundException {
    var user = findUserById(userId).orElseThrow(() -> new UserNotFoundException("id = " + userId));
    if (user.isEnabled() != enable) {
      user.setEnabled(enable);
      save(user);
      if (enable) {
        comeTChatService.activeUser(user.getId());
      } else {
        comeTChatService.inActiveUser(user.getId());
      }
    }
  }

  @Override
  public Page<User> findAllUsers(PageCriteria pageCriteria, FindUsersRequest request) {
    var pageable = PageCriteriaPageableMapper.toPageable(pageCriteria);
    return userRepository.findAllByConditions(request, pageable);
  }

  @Override
  public List<User> findAllByIds(Collection<Long> ids) {
    return userRepository.findAllByIdInAndEnabledIsTrue(ids);
  }

  @Transactional
  @Override
  public String uploadAvatar(LocalUser localUser, MultipartFile image) throws IOException {
    var user = localUser.getUser();
    String imageUrl = googleCloudFileUpload.uploadImage(image);
    user.setImageUrl(imageUrl);
    save(user);
    return imageUrl;
  }

  @Override
  public void updateMentorStatus(Long userId, MentorStatus status) {
    var user = findUserById(userId).orElseThrow(() -> new UserNotFoundException("id = " + userId));
    user.setStatus(status);

    // User become mentor
    if (MentorStatus.ACCEPTED.equals(status) && UserRole.ROLE_USER.equals(user.getRole())) {
      var roleMentor = roleRepository.findByName(UserRole.ROLE_MENTOR.name());
      user.setRoles(new HashSet<>(List.of(roleMentor)));
    } else if (MentorStatus.REJECTED.equals(status)
        && UserRole.ROLE_MENTOR.equals(user.getRole())) {
      var roleUser = roleRepository.findByName(UserRole.ROLE_USER.name());
      user.setRoles(new HashSet<>(List.of(roleUser)));
    }
    save(user);
  }

  @Override
  public BasicUserInfo findBasicUserInfoByUserId(Long id) {
    var user = findUserById(id).orElseThrow(() -> new UserNotFoundException("id = " + id));
    return UserMapper.toBasicUserInfo(user);
  }

  @Override
  public Map<Long, BasicUserInfo> findBasicUserInfos(List<Long> ids) {
    var prjList = userRepository.findBasicUserInfoByIdIn(ids);
    return prjList.stream()
        .collect(Collectors.toMap(BasicUserInfoProjection::getId, UserMapper::toBasicUserInfo));
  }

  private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
    existingUser.setFullName(oAuth2UserInfo.getName());
    existingUser.setImageUrl(oAuth2UserInfo.getImageUrl());
    return save(existingUser);
  }

  private SignUpRequest toUserRegistrationObject(
      String registrationId, OAuth2UserInfo oAuth2UserInfo) {
    return SignUpRequest.getBuilder()
        .addProviderUserID(oAuth2UserInfo.getId())
        .addFullName(oAuth2UserInfo.getName())
        .addEmail(oAuth2UserInfo.getEmail())
        .addSocialProvider(UserMapper.toSocialProvider(registrationId))
        .addPassword(defaultPassword) // FIXME:
        // change
        // it
        // to
        // a
        // random
        // password
        .build();
  }

  @Override
  public Optional<User> findUserById(Long id) {
    return userCache.findUserById(id);
  }

  @Override
  public LocalUser findLocalUserById(Long id) {
    var user = findUserById(id).orElse(null);
    if (user == null) {
      throw new UserNotFoundException("id = " + id);
    }
    return LocalUser.create(user, null, null, null);
  }

  @Override
  public Page<UserDetails> findAllUserProfile(
      PageCriteria pageCriteria, FindUsersRequest request, LocalUser localUser) {
    if (localUser == null || !UserRole.MANAGER_ROLES.contains(localUser.getUser().getRole())) {
      request.setEnabled(true);
    }

    var pageable = PageCriteriaPageableMapper.toPageable(pageCriteria);
    var response =
        userRepository.findAllByConditions(request, pageable).map(UserMapper::buildUserDetails);

    var favoriteMentors = new ArrayList();
    if (!Objects.isNull(localUser)) {
      if (localUser.getUser().getRole() == UserRole.ROLE_USER) {
        favoriteMentors =
            (ArrayList) favoriteMentorRepository.findAllByStudentId(localUser.getUserId());
        if (favoriteMentors.size() > 0) {
          Collections.sort(favoriteMentors, Comparator.comparing(FavoriteMentor::getMentorId));
          for (UserDetails user : response.getContent()) {
            var isLiked = isLiked(favoriteMentors, user.getId());
            user.setIsLiked(isLiked);
          }
        }
      }
    }

    return response;
  }

  private Boolean isLiked(ArrayList<FavoriteMentor> favoriteMentors, Long mentorId) {
    int left = 0;
    int right = favoriteMentors.size() - 1;
    while (right >= left) {
      int mid = left + (right - left) / 2;
      var id = favoriteMentors.get(mid).getMentorId();
      if (id == mentorId) return true;
      if (id > mentorId) {
        right = mid - 1;
      } else {
        left = mid + 1;
      }
    }
    return false;
  }

  @Override
  public UserDetails findUserProfileById(Long id, LocalUser localUser) {
    var userOpt = userCache.findUserById(id);
    if (userOpt.isEmpty()) {
      throw new UserNotFoundException("id = " + id);
    }
    var userDetails = UserMapper.buildUserDetails(userOpt.get());
    if (userOpt.get().getRole() == UserRole.ROLE_MENTOR) {
      if (!Objects.isNull(localUser)) {
        if (localUser.getUser().getRole() == UserRole.ROLE_USER) {
          var favoriteMentors =
              favoriteMentorRepository.findAllByStudentIdAndMentorId(localUser.getUserId(), id);
          if (favoriteMentors.size() > 0) {
            userDetails.setIsLiked(true);
          } else {
            userDetails.setIsLiked(false);
          }
        }
      }
    }
    return userDetails;
  }
}
