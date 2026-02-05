package org.handler.rule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.handler.model.Case;
import org.handler.model.enums.CaseStatus;
import org.handler.repository.CaseRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleScheduler {
    private final CaseRepository caseRepository;

    @Scheduled(cron = "0 0 * * * *")
    public void checkStaleCases() {
        log.info("StaleCaseRuleEvent received. Starting background check for stale cases...");

        LocalDateTime threshold = LocalDateTime.now().minusHours(48);
        List<Case> staleCases = caseRepository.findCasesWithLastProcessingActionBefore(CaseStatus.OPEN, threshold);

        for (Case staleCase : staleCases) {
            staleCase.setStatus(CaseStatus.IN_PROCESSING);
            caseRepository.save(staleCase);
            log.info("Case {} marked as IN_PROCESSING due to staleness", staleCase.getId());
        }

        log.info("Stale case background check completed. Total stale cases processed: {}", staleCases.size());
    }
}
