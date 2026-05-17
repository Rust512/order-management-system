package com.design.order_management_system.model.security;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Getter
@Builder
@RequiredArgsConstructor
public class PrincipalUser implements UserDetails {

    private final Long userId;
    private final String username;
    private final List<String> roles;

    @Setter
    private Instant expiryTime;

    public PrincipalUser(User user) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.roles = user.getRoles()
                .stream()
                .map(UserRoleMapping::getRole)
                .map(Role::getName)
                .toList();
    }

    @Override
    @NullMarked
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override
    @NullMarked
    public String getUsername() {
        return this.username;
    }

    @Override
    public @Nullable String getPassword() {
        return null;
    }
}
