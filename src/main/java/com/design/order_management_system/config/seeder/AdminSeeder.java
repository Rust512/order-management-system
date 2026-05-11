package com.design.order_management_system.config.seeder;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.model.security.User;
import com.design.order_management_system.repository.RoleRepository;
import com.design.order_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_USERNAME = "ADMIN";
    private static final String ADMIN_PASSWORD = "Admin@123";

    @Override
    @NullMarked
    public void run(String... args) {
        var user = User.builder()
                .username(ADMIN_USERNAME)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .build();
        var adminRole = roleRepository.findByName(CommonConstants.ROLE_ADMIN)
                .orElseThrow(() -> new IllegalStateException(RoleSeeder.ROLE_USER_WAS_NOT_SEEDED));
        user.addRole(adminRole);
        boolean exists = userRepository.existsByUsername(ADMIN_USERNAME);
        if (!exists) {
            userRepository.save(user);
        }
    }
}
