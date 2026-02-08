package com.dragons.monitoring;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SlowQueryFileReader {
  private final Path logPath;
  private final FileOffsetStore offsetStore;

  public SlowQueryFileReader(Path logPath, FileOffsetStore offsetStore) {
    this.logPath = logPath;
    this.offsetStore = offsetStore;
  }

  public List<String> readNewLines() throws IOException {
    List<String> lines = new ArrayList<>();

    if (!Files.exists(logPath)) {
      return lines;
    }

    try (RandomAccessFile raf = new RandomAccessFile(logPath.toFile(), "r")) {
      long lastOffset = offsetStore.getOffset();

      // 로그 로테이션 대응 (파일이 줄어든 경우)
      if (raf.length() < lastOffset) {
        lastOffset = 0;
      }

      raf.seek(lastOffset);

      String line;
      while ((line = raf.readLine()) != null) {
        lines.add(line);
      }

      offsetStore.update(raf.getFilePointer());
    }

    return lines;
  }
}
