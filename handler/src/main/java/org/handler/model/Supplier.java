package org.handler.model;

import jakarta.persistence.*;
import lombok.*;
import org.handler.model.enums.CaseSubtype;
import org.handler.model.enums.SupplierSource;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.*;

@Entity
@Table(name = "suppliers")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50)")
    private SupplierSource source;

    @Builder.Default
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "supplier_handled_case_subtypes",
            joinColumns = @JoinColumn(name = "supplier_id")
    )
    @Column(name = "case_subtype")
    private Set<CaseSubtype> handledCaseSubtypes = new HashSet<>();

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> metadata;

    @OneToMany(mappedBy = "supplier", fetch = FetchType.LAZY)
    private List<Case> cases = new ArrayList<>();
}
