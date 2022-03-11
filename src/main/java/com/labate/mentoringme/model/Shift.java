package com.labate.mentoringme.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Date;

@EntityListeners(AuditingEntityListener.class)
@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "shifts")
@SQLDelete(sql = "update shifts set is_deleted = true where id=?")
@Where(clause = "is_deleted = false")
public class Shift {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long classId;

  @Enumerated(EnumType.STRING)
  private DayOfWeek dayOfWeek;

  private LocalDateTime startTime;
  private LocalDateTime endTime;

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

  public void setStartTime(LocalDateTime startTime) {
    this.startTime = startTime;
    this.dayOfWeek = startTime.getDayOfWeek();
  }
}