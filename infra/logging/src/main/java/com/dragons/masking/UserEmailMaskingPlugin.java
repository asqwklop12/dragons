package com.dragons.masking;


import com.dragons.properties.MaskingProperties;
import com.dragons.properties.MaskingProperties.MaskingRule;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class UserEmailMaskingPlugin implements MaskingPlugin {
  private final ObjectMapper objectMapper;
  private final MaskingProperties properties;

  @Override
  public boolean supports(MaskingContext ctx) {
    return true;
  }

  @Override
  public String apply(String body) {
    MaskingRule rule = properties.rules().get("email");
    try {
      JsonNode root = objectMapper.readTree(body);
      maskNode(root, Pattern.compile(rule.match().pattern()),  rule.mask().value());
      return objectMapper.writeValueAsString(root);
    } catch (Exception e) {
      return body;
    }
  }
}
