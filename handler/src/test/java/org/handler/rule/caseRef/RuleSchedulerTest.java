package org.handler.rule.caseRef;

import org.handler.model.Case;
import org.handler.model.enums.CaseStatus;
import org.handler.repository.CaseRepository;
import org.handler.rule.RuleScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

public class RuleSchedulerTest {
    private CaseRepository caseRepository;
    private RuleScheduler ruleScheduler;

    @BeforeEach
    void setUp() {
        caseRepository = mock(CaseRepository.class);
        ruleScheduler = new RuleScheduler(caseRepository);
    }

    @Test
    void testCheckStaleCases_WhenStaleCasesPresent_ChangeCaseStatus() {
        Case staleCase1 = new Case();
        staleCase1.setId(1L);
        staleCase1.setStatus(CaseStatus.OPEN);

        Case staleCase2 = new Case();
        staleCase2.setId(2L);
        staleCase2.setStatus(CaseStatus.OPEN);

        List<Case> staleCases = List.of(staleCase1, staleCase2);

        when(caseRepository.findCasesWithLastProcessingActionBefore(eq(CaseStatus.OPEN), any()))
                .thenReturn(staleCases);

        ruleScheduler.checkStaleCases();

        staleCases.forEach(c -> {
            assert(c.getStatus() == CaseStatus.IN_PROCESSING);
        });

        verify(caseRepository, times(1)).findCasesWithLastProcessingActionBefore(eq(CaseStatus.OPEN), any());
        verify(caseRepository, times(1)).save(staleCase1);
        verify(caseRepository, times(1)).save(staleCase2);
    }

    @Test
    void testCheckStaleCases_WhenStaleCasesNotPresent_DontChangeCaseStatus() {
        when(caseRepository.findCasesWithLastProcessingActionBefore(eq(CaseStatus.OPEN), any()))
                .thenReturn(List.of());

        ruleScheduler.checkStaleCases();

        verify(caseRepository, times(1)).findCasesWithLastProcessingActionBefore(eq(CaseStatus.OPEN), any());
        verify(caseRepository, never()).save(any());
    }
}
