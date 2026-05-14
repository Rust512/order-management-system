package com.design.order_management_system.config;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.model.security.Role;
import com.design.order_management_system.model.security.User;
import com.design.order_management_system.repository.RoleRepository;
import com.design.order_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public static final String ROLE_USER_WAS_NOT_SEEDED = "ROLE_USER was not seeded";

    private static final String ADMIN_USERNAME = "ADMIN";
    private static final String ADMIN_PASSWORD = "Admin@123";

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

        var user = User.builder()
                .username(ADMIN_USERNAME)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .build();
        var adminRole = roleRepository.findByName(CommonConstants.ROLE_ADMIN)
                .orElseThrow(() -> new IllegalStateException(DataSeeder.ROLE_USER_WAS_NOT_SEEDED));
        user.addRole(adminRole);
        boolean exists = userRepository.existsByUsername(ADMIN_USERNAME);
        if (!exists) {
            userRepository.save(user);
        }
    }
}
