package org.handler.model;

import jakarta.persistence.*;
import lombok.*;
import org.handler.model.enums.SupplierSource;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private String supplierName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50)")
    private SupplierSource source;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> metadata;

    @OneToMany(mappedBy = "supplier")
    private List<Case> cases = new ArrayList<>();
}
