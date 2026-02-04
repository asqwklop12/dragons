package com.dragons.masking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class UserEmailMaskingPlugin implements MaskingPlugin {
  private final ObjectMapper objectMapper;

  @Override
  public String apply(String body) {
    try {
      JsonNode root = objectMapper.readTree(body);
      maskRecursive(root,"email","******");
      return objectMapper.writeValueAsString(root);
    } catch (Exception e) {
      return body;
    }
  }
}
