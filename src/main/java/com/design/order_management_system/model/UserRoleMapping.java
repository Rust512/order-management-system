package com.design.order_management_system.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "user_role_mappings")
public class UserRoleMapping {
    @EmbeddedId
    private UserRoleMappingKey id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId(value = "userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId(value = "roleId")
    @JoinColumn(name = "role_id")
    private Role role;
}
