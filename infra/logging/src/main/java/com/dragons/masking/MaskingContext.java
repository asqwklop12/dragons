package com.dragons.masking;

/**
 * 마스킹/로깅 플러그인들이 판단을 내리기 위해 참조하는
 * "요청/응답의 맥락(Context)" 정보 객체.
 *
 * ❗ 이 객체는 정책을 결정하지 않는다.
 * ❗ 오직 사실(Fact)만 담는다.
 *
 * - 어떤 요청인가?
 * - 누가 호출했는가?
 * - 어느 시점의 데이터인가?
 *
 * 판단 로직은 전부 Plugin 쪽에 위치한다.
 */
public record MaskingContext(
    /**
     * 호출 경로의 성격
     *
     * INTERNAL : 서비스 내부 호출
     * EXTERNAL : 외부 클라이언트 / 외부 시스템 호출
     *
     * → 외부 호출 여부에 따라 마스킹 강도, 로깅 정책이 달라질 수 있음
     */
    CallType callType,
    /**
     * API의 성격 분류
     *
     * PUBLIC   : 일반 사용자 대상 API
     * ADMIN    : 관리자 전용 API
     * INTERNAL : 시스템 간 통신용 API
     *
     * → 같은 외부 호출이라도 API 성격에 따라
     *   마스킹/노출 정책이 달라질 수 있음
     */
    ApiCategory apiCategory,
    /**
     * 실제 호출 주체의 역할
     *
     * USER   : 일반 사용자
     * ADMIN  : 관리자
     * SYSTEM : 배치 / 내부 서비스
     *
     * → 인증/인가 로직의 "결과"만 담는다.
     * → 권한 판단 로직은 Context 밖에서 수행되어야 함
     */
    CallerRole callerRole,
    /**
     * 인증 여부
     *
     * true  : 인증된 요청
     * false : 비인증 요청
     *
     * → CallerRole과 분리된 개념
     * → 비인증 + PUBLIC API 같은 케이스를 표현하기 위함
     */
    boolean authenticated,
    /**
     * 직렬화 단계
     *
     * BEFORE : 객체(Object) 상태
     * AFTER  : 문자열(String) 상태
     *
     * → 어떤 타입의 마스킹이 가능한지를 결정하는 기준
     *   (필드 단위 vs 문자열 기반)
     */
    SerializationPhase phase,
    /**
     * 요청 URI 경로
     *
     * 예) /api/v1/users/123
     *
     * → 특정 엔드포인트 예외 처리,
     *   경로 기반 마스킹 정책에 사용
     */
    String path,
    /**
     * Content-Type 헤더 값
     *
     * 예) application/json
     *
     * → JSON 파싱 가능 여부,
     *   바이너리 응답 스킵 판단 등에 사용
     */
    String contentType
) {
  public enum CallType { INTERNAL, EXTERNAL }
  public enum ApiCategory { PUBLIC, ADMIN, INTERNAL }
  public enum CallerRole { USER, ADMIN, SYSTEM }
  public enum SerializationPhase { BEFORE, AFTER }
}
