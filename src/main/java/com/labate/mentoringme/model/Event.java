package com.labate.mentoringme.model;

import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "events")
@SQLDelete(sql = "update events set is_deleted = true where id=?")
@Where(clause = "is_deleted = false")
public class Event {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "timetable_id")
  private Long timetableId;

  @Column(name = "mentorship_id")
  private Long mentorshipId;

  @Column(name = "shift_id")
  private Long shiftId;

  private String title;
  // private String content;

  private Date startTime;
  private Date endTime;

  private Integer type;

  @Builder.Default
  @Column(columnDefinition = "BIT", length = 1, nullable = false)
  private Boolean isDeleted = false;

  @CreatedDate
  @Column(name = "created", updatable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date createdDate;

  @LastModifiedDate
  @Column(name = "modified")
  @Temporal(TemporalType.TIMESTAMP)
  private Date modifiedDate;

  @AllArgsConstructor
  @Data
  public static class Basic {
    private Long shiftId;
    private Date startTime;
    private Date endTime;
  }
}
