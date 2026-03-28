package org.handler.model;

import jakarta.persistence.*;
import lombok.*;
import org.handler.model.enums.CaseStatus;
import org.handler.model.enums.CaseSubtype;
import org.handler.model.enums.CaseType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cases")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Case {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private CaseType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 150)
    private CaseSubtype subtype;

    @Column(nullable = false, length = 8)
    private String cpvCode;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime modifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50)")
    private CaseStatus status;

    @Column(nullable = false, length = 50)
    private String title;

    @OneToMany(mappedBy = "caseRef", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProcessingAction> processingActions = new ArrayList<>();

    @OneToMany(mappedBy = "caseRef", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CasePhoto> photos = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;
}
