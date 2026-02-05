package org.handler.rule.caseRef;

import org.handler.model.Case;
import org.handler.model.enums.CaseStatus;
import org.handler.model.enums.RuleType;
import org.handler.repository.CaseRepository;
import org.handler.rule.RuleContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RequestCaseCommentRuleTest {
    private CaseRepository caseRepository;
    private RequestCaseCommentRule requestCaseCommentRule;

    @BeforeEach
    void setUp() {
        caseRepository = mock(CaseRepository.class);
        requestCaseCommentRule = new RequestCaseCommentRule(caseRepository);
    }

    @Test
    void testProcessCase_WhenCaseWaitingForResponse_StatusChanged() {
        Long caseId = 1L;
        Case caseRef = createCase(caseId, CaseStatus.WAITING_FOR_USER_RESPONSE);

        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseRef));

        RuleContext ruleContext = new RuleContext(RuleType.REQUEST_CASE_COMMENT, Map.of("caseId", caseId));
        requestCaseCommentRule.execute(ruleContext);

        assertEquals(CaseStatus.READY_FOR_REVIEW, caseRef.getStatus());
        verify(caseRepository).save(caseRef);
    }

    @Test
    void testProcessCase_WhenCaseNotWaitingForResponse_StatusNotChanged() {
        Long caseId = 2L;
        Case caseRef = createCase(caseId, CaseStatus.OPEN);

        when(caseRepository.findById(caseId)).thenReturn(Optional.of(caseRef));

        RuleContext ruleContext = new RuleContext(RuleType.REQUEST_CASE_COMMENT, Map.of("caseId", caseId));
        requestCaseCommentRule.execute(ruleContext);

        assertEquals(CaseStatus.OPEN, caseRef.getStatus());
        verify(caseRepository, never()).save(any());
    }

    private Case createCase(Long id, CaseStatus status) {
        Case caseRef = new Case();
        caseRef.setId(id);
        caseRef.setStatus(status);
        return caseRef;
    }
}
