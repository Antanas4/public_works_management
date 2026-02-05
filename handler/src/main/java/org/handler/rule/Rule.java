package org.handler.rule;


public interface Rule {
    boolean match(RuleContext ruleContext);
    void execute(RuleContext ruleContext);
}
