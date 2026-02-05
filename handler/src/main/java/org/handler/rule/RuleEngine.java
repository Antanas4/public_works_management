package org.handler.rule;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RuleEngine {
    private final List<Rule> rules;

    public void process(RuleContext context) {
        for (Rule rule : rules) {
            if (rule.match(context)) {
                rule.execute(context);
            }
        }
    }
}
 