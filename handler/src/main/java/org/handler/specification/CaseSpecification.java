package org.handler.specification;

import org.handler.model.Case;
import org.handler.model.enums.CaseStatus;
import org.handler.model.enums.CaseType;
import org.springframework.data.jpa.domain.Specification;

public class CaseSpecification {
    public static Specification<Case> hasUserId(Long userId) {
        return (root, criteriaQuery, criteriaBuilder)
                -> criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Case> hasType(CaseType type){
        return (root, criteriaQuery, criteriaBuilder)
                -> criteriaBuilder.equal(root.get("type"), type);
    }

    public static Specification<Case> hasStatus(CaseStatus status){
        return (root, criteriaQuery, criteriaBuilder)
                -> criteriaBuilder.equal(root.get("status"), status);
    }

}
