package com.design.order_management_system.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class UserRoleMappingKey {
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "role_id")
    private Long roleId;
}
