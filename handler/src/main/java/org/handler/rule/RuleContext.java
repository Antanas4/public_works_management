package org.handler.rule;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.handler.model.enums.RuleType;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public class RuleContext {
    private final RuleType ruleType;
    private final Map<String, Object> data;
}
