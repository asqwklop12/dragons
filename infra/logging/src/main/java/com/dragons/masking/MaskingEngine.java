package com.dragons.masking;

import com.dragons.properties.MaskingProperties;
import com.dragons.properties.MaskingProperties.MaskingRule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaskingEngine {
  private final ObjectMapper objectMapper;
  private final MaskingProperties properties;

  public String mask(String body) {
    if (body == null || body.isBlank()) {
      return body;
    }

    try {
      JsonNode root = objectMapper.readTree(body);

      for (MaskingRule rule : properties.rules().values()) {
        applyRule(root, rule);
      }

      return objectMapper.writeValueAsString(root);
    } catch (JsonProcessingException e) {
      return body;
    }
  }

  private void applyRule(JsonNode node, MaskingRule rule) {
    if (node.isObject()) {
      ObjectNode obj = (ObjectNode) node;
      for (Entry<String, JsonNode> entry : node.properties()) {
        String fieldName = entry.getKey();
        JsonNode value = entry.getValue();

        if (matches(rule, fieldName, value)) {
          obj.put(fieldName, rule.mask().value());
        } else {
          applyRule(value, rule);
        }
      }
    } else if (node.isArray()) {
      node.forEach(child -> applyRule(child, rule));
    }
  }

  private boolean matches(MaskingRule rule, String fieldName, JsonNode value) {
    Pattern pattern = Pattern.compile(rule.match().pattern());
    return switch (rule.match().type()) {
      case FIELD_REGEX -> pattern.matcher(fieldName).matches();

      case VALUE_REGEX -> value.isTextual()
          && pattern.matcher(value.asText()).matches();
    };
  }
}
