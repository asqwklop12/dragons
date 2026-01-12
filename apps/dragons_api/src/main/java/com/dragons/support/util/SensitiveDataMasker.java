package com.dragons.support.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SensitiveDataMasker {
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final Set<String> SENSITIVE_FIELDS = Set.of(
      "password", "email", "cvc", "cardNumber"
  );
  private static final String MASK = "***MASKED***";

  public static String maskSensitiveData(String jsonBody) {
    if (jsonBody == null || jsonBody.isEmpty()) {
      return jsonBody;
    }

    try {
      JsonNode rootNode = objectMapper.readTree(jsonBody);
      maskNode(rootNode);
      return objectMapper.writeValueAsString(rootNode);
    } catch (Exception e) {
      log.warn("Failed to parse JSON for masking, returning original: {}", e.getMessage());
      return jsonBody;
    }
  }
  private static void maskNode(JsonNode node) {
    if (node.isObject()) {
      ObjectNode objectNode = (ObjectNode) node;
      objectNode.fields().forEachRemaining(entry -> {
        String fieldName = entry.getKey();
        JsonNode fieldValue = entry.getValue();

        if (SENSITIVE_FIELDS.stream()
            .anyMatch(sensitive -> fieldName.toLowerCase().contains(sensitive.toLowerCase()))) {
          objectNode.put(fieldName, MASK);
        } else if (fieldValue.isObject() || fieldValue.isArray()) {
          maskNode(fieldValue);
        }
      });
    } else if (node.isArray()) {
      node.forEach(SensitiveDataMasker::maskNode);
    }
  }
}
