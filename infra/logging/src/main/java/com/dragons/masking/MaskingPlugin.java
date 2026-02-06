package com.dragons.masking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public interface MaskingPlugin {
  String apply(String body);

  boolean supports(MaskingContext ctx);

  default void maskRecursive(JsonNode node, String name, String masking) {
    if (node.isObject()) {
      ObjectNode obj = (ObjectNode) node;
      for (Map.Entry<String, JsonNode> entry : node.properties()) {

        String fieldName = entry.getKey();
        JsonNode value = entry.getValue();

        if (name.equalsIgnoreCase(fieldName) && value.isTextual()) {
          obj.put(fieldName, masking);
        } else {
          maskRecursive(value, name, masking);
        }
      }

    } else if (node.isArray()) {
      for (JsonNode child : node) {
        maskRecursive(child, name, masking);
      }
    }
  }

  default void maskNode(JsonNode node, Pattern pattern, String masking) {
    if (node.isObject()) {
      ObjectNode obj = (ObjectNode) node;

      for (Entry<String, JsonNode> entry : node.properties()) {
        JsonNode value = entry.getValue();

        if (value.isTextual()) {
          String text = value.asText();
          // 값 내용으로 민감 정보 판단
          if (pattern.matcher(text).matches()) {
            obj.put(entry.getKey(), masking);
          }
        } else {
          maskNode(value, pattern, masking);
        }
      }
    } else if (node.isArray()) {
      for (JsonNode child : node) {
        maskNode(child, pattern, masking);
      }
    }
  }
}
