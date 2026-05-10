package com.design.order_management_system.config.seeder;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.model.security.Role;
import com.design.order_management_system.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public static final String ROLE_USER_WAS_NOT_SEEDED = "ROLE_USER was not seeded";

    @Override
    @NullMarked
    public void run(String... args) {
        List<String> requiredRoles = List.of(CommonConstants.ROLE_USER, CommonConstants.ROLE_ADMIN);
        Set<String> existingRoles = roleRepository.findAllByNameIn(requiredRoles)
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        requiredRoles.stream()
                .filter(entry -> !existingRoles.contains(entry))
                .map(entry -> Role.builder().name(entry).build())
                .forEach(roleRepository::save);
    }
}
