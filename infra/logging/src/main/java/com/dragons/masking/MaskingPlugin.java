package com.dragons.masking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;

public interface MaskingPlugin {
  String apply(String body);

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
}
