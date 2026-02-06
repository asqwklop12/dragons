package com.dragons.masking;

import com.dragons.properties.MaskingProperties;
import com.dragons.properties.MaskingProperties.MaskingRule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MaskingEngine {
  private final ObjectMapper objectMapper;
  private final MaskingProperties properties;
  private final Map<String, Pattern> compiledPatterns;

  public MaskingEngine(ObjectMapper objectMapper, MaskingProperties properties) {
    this.objectMapper = objectMapper;
    this.properties = properties;
    this.compiledPatterns = properties.rules().entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> Pattern.compile(e.getValue().match().pattern())
        ));
  }

  public String mask(String body) {
    if (body == null || body.isBlank()) {
      return body;
    }

    try {
      JsonNode root = objectMapper.readTree(body);

      for (var entry : properties.rules().entrySet()) {
        applyRule(root, entry.getValue(), compiledPatterns.get(entry.getKey()));
      }

      return objectMapper.writeValueAsString(root);
    } catch (JsonProcessingException e) {
      log.warn("Failed to mask body, returning original. Consider fail-closed strategy.", e);
      return body;
    }
  }

  private void applyRule(JsonNode node, MaskingRule rule, Pattern pattern) {
    if (node.isObject()) {
      ObjectNode obj = (ObjectNode) node;
      for (Entry<String, JsonNode> entry : node.properties()) {
        String fieldName = entry.getKey();
        JsonNode value = entry.getValue();

        if (matches(rule, fieldName, value, pattern)) {
          obj.put(fieldName, rule.mask().value());
        } else {
          applyRule(value, rule, pattern);
        }
      }
    } else if (node.isArray()) {
      node.forEach(child -> applyRule(child, rule, pattern));
    }
  }

  private boolean matches(MaskingRule rule, String fieldName, JsonNode value, Pattern pattern) {
    return switch (rule.match().type()) {
      case FIELD_REGEX -> pattern.matcher(fieldName).matches();

      case VALUE_REGEX -> value.isTextual()
          && pattern.matcher(value.asText()).matches();
    };
  }
}
