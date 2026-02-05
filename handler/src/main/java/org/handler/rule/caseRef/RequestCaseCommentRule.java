package org.handler.rule.caseRef;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.handler.model.Case;
import org.handler.model.enums.CaseStatus;
import org.handler.model.enums.RuleType;
import org.handler.repository.CaseRepository;
import org.handler.rule.Rule;
import org.handler.rule.RuleContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class RequestCaseCommentRule implements Rule {
    private final CaseRepository caseRepository;

    @Override
    public boolean match(RuleContext ruleContext) {
        return ruleContext.getRuleType() == RuleType.REQUEST_CASE_COMMENT;
    }

    @Override
    public void execute(RuleContext ruleContext) {
        Long caseId = (Long) ruleContext.getData().get("caseId");

        Optional<Case> caseRef = caseRepository.findById(caseId);

        caseRef.ifPresent(caseEntity -> {
            if (caseEntity.getStatus() == CaseStatus.WAITING_FOR_USER_RESPONSE) {
                caseEntity.setStatus(CaseStatus.READY_FOR_REVIEW);
                caseRepository.save(caseEntity);
                log.info("Case {} updated to READY_FOR_REVIEW", caseId);
            }
        });
    }
}
