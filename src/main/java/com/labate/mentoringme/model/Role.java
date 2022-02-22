package com.labate.mentoringme.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/** The persistent class for the role database table. */
@EntityListeners(AuditingEntityListener.class)
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "roles")
public class Role implements Serializable {
  private static final long serialVersionUID = 1L;
  public static final String USER = "USER";
  public static final String ROLE_ADMIN = "ROLE_ADMIN";
  public static final String ROLE_MODERATOR = "ROLE_MODERATOR";
  public static final String ROLE_MENTOR = "ROLE_MENTOR";
  public static final String ROLE_USER = "ROLE_USER";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  private String name;

  // bidirectional many-to-many association to User
  @ManyToMany(mappedBy = "roles")
  private Set<User> users;

  public Role(String name) {
    this.name = name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Role role = (Role) obj;
    return role.equals(role.name);
  }

  @Override
  public String toString() {
    return "Role [name=" + name + "]" + "[id=" + id + "]";
  }
}
