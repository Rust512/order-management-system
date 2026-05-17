package com.design.order_management_system.model.domain;

import com.design.order_management_system.model.enumeration.OperationType;
import com.design.order_management_system.model.security.User;
import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "product_audit_entries",
        uniqueConstraints = @UniqueConstraint(
                name = "unique_product_audit_entries_product_id_version",
                columnNames = "product_id, version"
        )
)
public class ProductAuditEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            updatable = false,
            check = @CheckConstraint(
                    name = "version_positive",
                    constraint = "version > 0"
            )
    )
    private Long version;

    @Column(nullable = false, updatable = false)
    private String productName;

    @Column(nullable = false, updatable = false)
    private BigDecimal price;

    @Column(nullable = false, updatable = false)
    private Long stock;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private OperationType operationType;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            updatable = false
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            updatable = false
    )
    private Product product;
}
