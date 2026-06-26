package com.design.order_management_system.model.domain;

import com.design.order_management_system.model.enumeration.OrderOperation;
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
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
    name = "order_audit_entries",
    uniqueConstraints =
        @UniqueConstraint(
            name = "unique_order_audit_entries_order_id_version",
            columnNames = {"order_id", "version"}))
public class OrderAuditEntry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(
      nullable = false,
      updatable = false,
      check = @CheckConstraint(name = "order_version_positive", constraint = "version > 0"))
  private Long version;

  @Enumerated(value = EnumType.STRING)
  @Column(nullable = false, updatable = false)
  private OrderOperation operation;

  @JdbcTypeCode(value = SqlTypes.JSON)
  @Column(columnDefinition = "JSONB")
  private OrderSnapshot snapshot;

  @CreationTimestamp
  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, updatable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false, updatable = false)
  private Order order;
}
