package com.dragons.masking;

public interface MaskingPlugin {
  PluginPhase phase();
  /**
   * 같은 phase 안에서의 순서. 숫자가 싫으면 enum 단계를 더 쪼개도 됨.
   */
  default int order() { return 0; }

  /**
   * 적용 가능 여부. context만 보고 판단 (순수 함수처럼)
   */
  boolean supports(MaskingContext ctx);

  /**
   * 누적 적용. 앞/뒤 플러그인 존재를 가정하고 "부분 변경"만.
   */
  String apply(String body, MaskingContext ctx);
}
