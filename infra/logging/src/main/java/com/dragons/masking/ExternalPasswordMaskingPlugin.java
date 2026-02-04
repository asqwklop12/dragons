package com.dragons.masking;

import com.dragons.masking.MaskingContext.CallType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ExternalPasswordMaskingPlugin implements MaskingPlugin {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  private static final Set<String> SENSITIVE_KEYS = Set.of(
      "password",
      "pwd",
      "secret",
      "token",
      "accessToken"
  );

  @Override
  public PluginPhase phase() {
    return PluginPhase.MASKING;
  }

  @Override
  public int order() {
    return 2;
  }

  @Override
  public boolean supports(MaskingContext ctx) {
    return ctx.callType() == CallType.INTERNAL
        && ctx.callerRole() == MaskingContext.CallerRole.USER
        && ctx.phase() == MaskingContext.SerializationPhase.AFTER
        && ctx.contentType() != null
        && ctx.contentType().contains("application/json");
  }

  @Override
  public String apply(String body, MaskingContext ctx) {

    try {
      JsonNode root = objectMapper.readTree(body);
      maskPasswordRecursive(root);   // void
      return objectMapper.writeValueAsString(root);
    } catch (Exception e) {
      return body;
    }
  }

  private static void maskPasswordRecursive(JsonNode node) {
    if (node.isObject()) {
      ObjectNode obj = (ObjectNode) node;
      Iterator<Entry<String, JsonNode>> fields = obj.fields();

      while (fields.hasNext()) {
        Entry<String, JsonNode> entry = fields.next();
        String fieldName = entry.getKey();
        JsonNode value = entry.getValue();

        if (SENSITIVE_KEYS.contains(fieldName.toLowerCase()) && value.isTextual()) {
          obj.put(fieldName, "*******");
        } else {
          maskPasswordRecursive(value);
        }
      }

    } else if (node.isArray()) {
      for (JsonNode child : node) {
        maskPasswordRecursive(child);
      }
    }
  }
}
