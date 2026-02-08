package com.dragons.monitoring;

import lombok.Getter;

@Getter
public class FileOffsetStore {
  private long offset = 0;

  public void update(long newOffset) {
    this.offset = newOffset;
  }

  public void reset() {
    this.offset = 0;
  }
}
