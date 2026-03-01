package com.dragons.monitoring.slow_query;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
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
        offsetStore.reset();
      }

      raf.seek(lastOffset);

      // RandomAccessFile.readLine() 직접 사용 (중첩 스트림 제거!)
      String line;
      while ((line = raf.readLine()) != null) {
        // readLine()은 ISO-8859-1로 읽으므로 UTF-8로 변환
        lines.add(new String(line.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8));
      }

      // RandomAccessFile이 아직 열려있으므로 정상 동작!
      offsetStore.update(raf.getFilePointer());
    }

    return lines;
  }
}
