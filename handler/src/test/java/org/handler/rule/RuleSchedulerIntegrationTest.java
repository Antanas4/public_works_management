package org.handler.rule;

import org.handler.model.Case;
import org.handler.model.ProcessingAction;
import org.handler.model.User;
import org.handler.model.enums.CaseStatus;
import org.handler.model.enums.CaseSubtype;
import org.handler.model.enums.CaseType;
import org.handler.model.enums.ProcessingStatus;
import org.handler.model.enums.UserType;
import org.handler.repository.CaseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.persistence.EntityManager;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ContextConfiguration(classes = RuleSchedulerIntegrationTest.TestApplication.class)
class RuleSchedulerIntegrationTest {

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void checkStaleCases_updatesOnlyOpenCasesOlderThan48Hours() {
        Case staleOpenCase = saveCaseWithActionTimestamp(
                "stale-open-case",
                CaseStatus.OPEN,
                LocalDateTime.now().minusHours(49)
        );

        Case freshOpenCase = saveCaseWithActionTimestamp(
                "fresh-open-case",
                CaseStatus.OPEN,
                LocalDateTime.now().minusHours(2)
        );

        Case staleClosedCase = saveCaseWithActionTimestamp(
                "stale-closed-case",
                CaseStatus.CLOSED,
                LocalDateTime.now().minusHours(72)
        );

        RuleScheduler ruleScheduler = new RuleScheduler(caseRepository);
        ruleScheduler.checkStaleCases();

        Case reloadedStaleOpen = caseRepository.findById(staleOpenCase.getId()).orElseThrow();
        Case reloadedFreshOpen = caseRepository.findById(freshOpenCase.getId()).orElseThrow();
        Case reloadedStaleClosed = caseRepository.findById(staleClosedCase.getId()).orElseThrow();

        assertEquals(CaseStatus.IN_PROCESSING, reloadedStaleOpen.getStatus());
        assertEquals(CaseStatus.OPEN, reloadedFreshOpen.getStatus());
        assertEquals(CaseStatus.CLOSED, reloadedStaleClosed.getStatus());
    }

    private Case saveCaseWithActionTimestamp(String suffix, CaseStatus status, LocalDateTime actionCreatedAt) {
        User user = User.builder()
                .username("user-" + suffix)
                .password("password")
                .email("user-" + suffix + "@example.com")
                .type(UserType.CLIENT)
                .build();
        entityManager.persist(user);

        Case caseEntity = Case.builder()
                .title("Case " + suffix)
                .type(CaseType.ENVIRONMENT)
                .subtype(CaseSubtype.WASTE_MANAGEMENT)
                .cpvCode(CaseSubtype.WASTE_MANAGEMENT.getCpvCode())
                .status(status)
                .user(user)
                .processingActions(new ArrayList<>())
                .build();

        ProcessingAction action = ProcessingAction.builder()
                .status(ProcessingStatus.DATA_PROVIDED)
                .parameters(Map.of("description", "test"))
                .caseRef(caseEntity)
                .build();

        caseEntity.getProcessingActions().add(action);

        Case savedCase = caseRepository.saveAndFlush(caseEntity);

        jdbcTemplate.update(
                "UPDATE processing_actions SET created_at = ? WHERE id = ?",
                Timestamp.valueOf(actionCreatedAt),
                savedCase.getProcessingActions().getFirst().getId()
        );

        return savedCase;
    }

    @SpringBootApplication
    @EntityScan(basePackages = "org.handler.model")
    @EnableJpaRepositories(basePackages = "org.handler.repository")
    static class TestApplication {
    }
}
