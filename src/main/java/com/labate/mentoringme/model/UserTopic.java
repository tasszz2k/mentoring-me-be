// package com.labate.mentoringme.model;
//
// import lombok.*;
// import org.springframework.data.annotation.CreatedDate;
// import org.springframework.data.annotation.LastModifiedDate;
// import org.springframework.data.jpa.domain.support.AuditingEntityListener;
//
// import javax.persistence.*;
// import java.util.Date;
//
// @Builder
// @AllArgsConstructor
// @EntityListeners(AuditingEntityListener.class)
// @Entity
// @NoArgsConstructor
// @Getter
// @Setter
// @Table(name = "users_topics")
// public class UserTopic {
//   @Id
//   @GeneratedValue(strategy = GenerationType.IDENTITY)
//   private Long id;
//
//   @Embedded private UserLikePost.Key key;
//
//   @Builder.Default
//   @Column(columnDefinition = "BIT", length = 1, nullable = false)
//   private Boolean isDeleted = false;
//
//   @CreatedDate
//   @Column(name = "created", updatable = false)
//   @Temporal(TemporalType.TIMESTAMP)
//   private Date createdDate;
//
//   @LastModifiedDate
//   @Column(name = "modified")
//   @Temporal(TemporalType.TIMESTAMP)
//   private Date modifiedDate;
//
//   @Data
//   @NoArgsConstructor
//   @AllArgsConstructor
//   @Embeddable
//   public static class Key {
//     private Long postId;
//     private Long userId;
//   }
// }
