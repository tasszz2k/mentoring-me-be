package com.labate.mentoringme.repository;

import com.labate.mentoringme.dto.projection.BasicUserInfoProjection;
import com.labate.mentoringme.dto.request.FindUsersRequest;
import com.labate.mentoringme.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  User findByEmail(String email);

  boolean existsByEmail(String email);

  @Query(
      value =
          "SELECT DISTINCT user "
              + "FROM User user "
              + "JOIN user.roles role "
              + "JOIN user.userProfile userProfile "
              + "LEFT JOIN userProfile.categories category "
              + "LEFT JOIN userProfile.address address "
              + "LEFT JOIN address.province province "
              + "LEFT JOIN address.district district "
              + "WHERE (:#{#request.getRoleName()} IS NULL OR role.name = :#{#request.getRoleName()}) "
              + "AND (:#{#request.mentorStatus} IS NULL OR user.status = :#{#request.mentorStatus}) "
              + "AND (:#{#request.enabled} IS NULL OR user.enabled = :#{#request.enabled}) "
              + "AND (COALESCE(:#{#request.genders}, NULL) IS NULL OR userProfile.genderValue IN (:#{#request.getGenderValues()})) "
              + "and (:#{#request.minPrice} is null or userProfile.price >= :#{#request.minPrice}) "
              + "and (:#{#request.maxPrice} is null or userProfile.price <= :#{#request.maxPrice}) "
              + "and (:#{#request.isOfflineStudy} is null or userProfile.isOfflineStudy = :#{#request.isOfflineStudy}) "
              + "and (:#{#request.isOnlineStudy} is null or userProfile.isOnlineStudy = :#{#request.isOnlineStudy}) "
              + "and (:#{#request.ratingFrom} is null or userProfile.rating >= :#{#request.ratingFrom}) "
              + "AND (COALESCE(:#{#request.categoryIds}, NULL) IS NULL OR category.id IN (:#{#request.categoryIds})) "
              + "AND (COALESCE(:#{#request.addressIds}, NULL) IS NULL "
              + " OR address.id IN (:#{#request.addressIds})"
              + " OR province.id IN (:#{#request.addressIds})"
              + " OR district.id IN (:#{#request.addressIds})"
              + ") "
              + "and (:#{#request.keyword} is null "
              + "or user.fullName like %:#{#request.keyword}% "
              + "or user.email like %:#{#request.keyword}% "
              + "or user.phoneNumber like %:#{#request.keyword}% "
              + "or userProfile.school like %:#{#request.keyword}% "
              + "or address.name like %:#{#request.keyword}% "
              + "or province.name like %:#{#request.keyword}% "
              + "or district.name like %:#{#request.keyword}% "
              + ") ",
      countQuery =
          "SELECT COUNT (DISTINCT user) "
              + "FROM User user "
              + "JOIN user.roles role "
              + "JOIN user.userProfile userProfile "
              + "LEFT JOIN userProfile.categories category "
              + "LEFT JOIN userProfile.address address "
              + "LEFT JOIN address.province province "
              + "LEFT JOIN address.district district "
              + "WHERE (:#{#request.getRoleName()} IS NULL OR role.name = :#{#request.getRoleName()}) "
              + "AND (:#{#request.mentorStatus} IS NULL OR user.status = :#{#request.mentorStatus}) "
              + "AND (:#{#request.enabled} IS NULL OR user.enabled = :#{#request.enabled}) "
              + "AND (COALESCE(:#{#request.genders}, NULL) IS NULL OR userProfile.genderValue IN (:#{#request.getGenderValues()})) "
              + "and (:#{#request.minPrice} is null or userProfile.price >= :#{#request.minPrice}) "
              + "and (:#{#request.maxPrice} is null or userProfile.price <= :#{#request.maxPrice}) "
              + "and (:#{#request.isOfflineStudy} is null or userProfile.isOfflineStudy = :#{#request.isOfflineStudy}) "
              + "and (:#{#request.isOnlineStudy} is null or userProfile.isOnlineStudy = :#{#request.isOnlineStudy}) "
              + "and (:#{#request.ratingFrom} is null or userProfile.rating >= :#{#request.ratingFrom}) "
              + "AND (COALESCE(:#{#request.categoryIds}, NULL) IS NULL OR category.id IN (:#{#request.categoryIds})) "
              + "AND (COALESCE(:#{#request.addressIds}, NULL) IS NULL "
              + " OR address.id IN (:#{#request.addressIds})"
              + " OR province.id IN (:#{#request.addressIds})"
              + " OR district.id IN (:#{#request.addressIds})"
              + ") "
              + "and (:#{#request.keyword} is null "
              + "or user.fullName like %:#{#request.keyword}% "
              + "or user.email like %:#{#request.keyword}% "
              + "or user.phoneNumber like %:#{#request.keyword}% "
              + "or userProfile.school like %:#{#request.keyword}% "
              + "or address.name like %:#{#request.keyword}% "
              + "or province.name like %:#{#request.keyword}% "
              + "or district.name like %:#{#request.keyword}% "
              + ") ")
  Page<User> findAllByConditions(@Param("request") FindUsersRequest request, Pageable pageable);

  List<User> findAllByIdInAndEnabledIsTrue(Collection<Long> userIds);

  @Query(
      value =
          "SELECT users.id             AS id,\n"
              + "       users.email          AS email,\n"
              + "       users.full_name      AS fullName,\n"
              + "       users.phone_number   AS phoneNumber,\n"
              + "       users.image_url      AS imageUrl,\n"
              + "       user_profiles.gender AS gender,\n"
              + "       roles.name           AS roles\n"
              + "\n"
              + "FROM users\n"
              + "         LEFT JOIN user_profiles ON users.profile_id = user_profiles.id\n"
              + "         LEFT JOIN users_roles ON users_roles.user_id = users.id\n"
              + "         LEFT JOIN roles ON roles.id = users_roles.role_id\n"
              + "WHERE users.id = :userId",
      nativeQuery = true)
  BasicUserInfoProjection findBasicUserInfoById(Long userId);

  @Query(
      value =
          "SELECT users.id             AS id,\n"
              + "       users.email          AS email,\n"
              + "       users.full_name      AS fullName,\n"
              + "       users.phone_number   AS phoneNumber,\n"
              + "       users.image_url      AS imageUrl,\n"
              + "       user_profiles.gender AS gender,\n"
              + "       roles.name           AS roles\n"
              + "\n"
              + "FROM users\n"
              + "         LEFT JOIN user_profiles ON users.profile_id = user_profiles.id\n"
              + "         LEFT JOIN users_roles ON users_roles.user_id = users.id\n"
              + "         LEFT JOIN roles ON roles.id = users_roles.role_id\n"
              + "WHERE users.id IN (:userIds)",
      nativeQuery = true)
  List<BasicUserInfoProjection> findBasicUserInfoByIdIn(List<Long> userIds);

  @Query("SELECT u.id FROM User u WHERE u.email = :email")
  Long findUserIdByEmail(String email);
}
