package com.dragons.domain.shedlock;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.ZonedDateTime;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shedlock")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)

public class ShedLock {
  @Id
  @Column(name = "name", length = 64, nullable = false)
  private String name;

  // DB에 따라 TIMESTAMP(3) 지원하면 아래처럼 columnDefinition 써도 됨
  @Column(name = "lock_until", nullable = false)
  private ZonedDateTime lockUntil;

  @Column(name = "locked_at", nullable = false)
  private ZonedDateTime lockedAt;

  @Column(name = "locked_by", length = 255, nullable = false)
  private String lockedBy;

}
