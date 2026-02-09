package com.dragons.slow_query;

import java.util.concurrent.atomic.AtomicLong;


public class FileOffsetStore {
  private final AtomicLong offset = new AtomicLong(0);

  public long getOffset() {
    return offset.get();
  }

  public void update(long newOffset) {
    this.offset.set(newOffset);
  }

  public void reset() {
    this.offset.set(0);
  }
}
