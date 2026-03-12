package com.dragons.lock;

import static org.assertj.core.api.Assertions.assertThat;

import com.dragons.constant.Constants.Lock;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.script.DefaultRedisScript;

class RedisLockLuaScriptCorrectnessTest extends RedisIntegrationTestSupport {

  private final DefaultRedisScript<Long> unlockScript =
      new DefaultRedisScript<>(Lock.LOCK_SCRIPT, Long.class);

  @Test
  @DisplayName("다른 토큰으로는 Redis 락을 해제할 수 없다")
  void shouldNotReleaseLockWhenTokenDoesNotMatch() {
    // given
    String key = nextLockKey("lock:payment-confirm");
    String ownerToken = "owner-token";
    String otherToken = "other-token";
    redisTemplate.opsForValue().set(key, ownerToken, Duration.ofSeconds(5));

    // when
    Long released = redisTemplate.execute(unlockScript, List.of(key), otherToken);

    // then
    assertThat(released).isZero();
    assertThat(redisTemplate.opsForValue().get(key)).isEqualTo(ownerToken);
  }

  @Test
  @DisplayName("같은 토큰일 때만 Redis 락이 해제된다")
  void shouldReleaseLockWhenTokenMatches() {
    // given
    String key = nextLockKey("lock:payment-confirm");
    String ownerToken = "owner-token";
    redisTemplate.opsForValue().set(key, ownerToken, Duration.ofSeconds(5));

    // when
    Long released = redisTemplate.execute(unlockScript, List.of(key), ownerToken);

    // then
    assertThat(released).isEqualTo(1L);
    assertThat(redisTemplate.hasKey(key)).isFalse();
  }
}
