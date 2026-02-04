package com.dragons.masking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class UserEmailMaskingPlugin implements MaskingPlugin {
  private final ObjectMapper objectMapper;

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

  @Override
  public String apply(String body) {
    try {
      JsonNode root = objectMapper.readTree(body);
      maskNode(root, EMAIL_PATTERN, "***MASKED***");
      return objectMapper.writeValueAsString(root);
    } catch (Exception e) {
      return body;
    }
  }
}
