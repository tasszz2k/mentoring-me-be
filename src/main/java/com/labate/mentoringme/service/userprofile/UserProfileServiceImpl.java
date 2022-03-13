package com.labate.mentoringme.service.userprofile;

import com.labate.mentoringme.constant.Gender;
import com.labate.mentoringme.constant.UserRole;
import com.labate.mentoringme.dto.model.LocalUser;
import com.labate.mentoringme.dto.request.PartialUpdateUserProfileRequest;
import com.labate.mentoringme.dto.request.UpdateUserProfileRequest;
import com.labate.mentoringme.exception.UserNotFoundException;
import com.labate.mentoringme.model.UserProfile;
import com.labate.mentoringme.repository.UserProfileRepository;
import com.labate.mentoringme.service.address.AddressService;
import com.labate.mentoringme.service.category.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserProfileServiceImpl implements UserProfileService {
  private final UserProfileRepository userProfileRepository;
  private final AddressService addressService;
  private final CategoryService categoryService;

  @Override
  public void save(UserProfile userProfile) {
    userProfileRepository.save(userProfile);
  }

  @Override
  public void partialUpdateProfile(LocalUser localUser, PartialUpdateUserProfileRequest request) {
    var userProfile =
        userProfileRepository
            .findById(request.getId())
            .orElseThrow(() -> new UserNotFoundException("id = " + request.getId()));

    checkPermissionToUpdate(localUser.getUser().getId(), localUser);

    String fullName = request.getFullName();
    String phoneNumber = request.getPhoneNumber();
    String imageUrl = request.getImageUrl();
    Gender gender = request.getGender();
    Date dob = request.getDob();
    String school = request.getSchool();
    Long addressId = request.getAddressId();
    String detailAddress = request.getDetailAddress();
    String bio = request.getBio();
    Float price = request.getPrice();
    Boolean isOnlineStudy = request.getIsOnlineStudy();
    Boolean isOfflineStudy = request.getIsOfflineStudy();
    List<Long> categoryIds = request.getCategoryIds();

    if (fullName != null) userProfile.getUser().setFullName(fullName);
    if (phoneNumber != null) userProfile.getUser().setPhoneNumber(phoneNumber);
    if (imageUrl != null) userProfile.getUser().setImageUrl(imageUrl);
    if (gender != null) userProfile.setGender(gender);
    if (dob != null) userProfile.setDob(dob);
    if (school != null) userProfile.setSchool(school);
    if (addressId != null) userProfile.setAddress(addressService.findById(addressId));
    if (detailAddress != null) userProfile.setDetailAddress(detailAddress);
    if (bio != null) userProfile.setBio(bio);
    if (price != null) userProfile.setPrice(price);
    if (isOnlineStudy != null) userProfile.setIsOnlineStudy(isOnlineStudy);
    if (isOfflineStudy != null) userProfile.setIsOfflineStudy(isOfflineStudy);
    if (categoryIds != null)
      userProfile.setCategories(new HashSet<>(categoryService.findByIds(categoryIds)));

    userProfileRepository.save(userProfile);
  }

  public void checkPermissionToUpdate(Long id, LocalUser localUser) {
    var userId = localUser.getUser().getId();
    var role = localUser.getUser().getRole();

    if (!userId.equals(id) && !UserRole.MANAGER_ROLES.contains(role)) {
      throw new AccessDeniedException("You are not allowed to update this profile");
    }
  }
}
