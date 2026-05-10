package com.design.order_management_system.config.seeder;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.model.security.Role;
import com.design.order_management_system.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public static final String ROLE_USER_WAS_NOT_SEEDED = "ROLE_USER was not seeded";

    @Override
    @NullMarked
    public void run(String... args) {
        var userRole = Role.builder()
                .name(CommonConstants.ROLE_USER)
                .build();
        var adminRole = Role.builder()
                .name(CommonConstants.ROLE_ADMIN)
                .build();

        var roleMap = Map.of(CommonConstants.ROLE_USER, userRole, CommonConstants.ROLE_ADMIN, adminRole);

        roleMap.entrySet()
                .stream()
                .filter(entry -> !roleRepository.existsByName(entry.getKey()))
                .forEach(entry -> roleRepository.save(entry.getValue()));
    }
}
