package com.design.order_management_system.service;

import com.design.order_management_system.config.DataSeeder;
import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.converter.CreateUserRequestToUser;
import com.design.order_management_system.converter.UserToUserResponse;
import com.design.order_management_system.dto.request.CreateUserRequest;
import com.design.order_management_system.dto.response.UserResponse;
import com.design.order_management_system.model.security.PrincipalUser;
import com.design.order_management_system.exception.DuplicateResourceException;
import com.design.order_management_system.exception.ResourceNotFoundException;
import com.design.order_management_system.model.security.User;
import com.design.order_management_system.repository.RoleRepository;
import com.design.order_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserToUserResponse userToUserResponse;
    private final CreateUserRequestToUser createUserRequestToUser;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse userRegistration(CreateUserRequest createUserRequest) {
        String username = createUserRequest.getUsername();
        log.info("User registration requested; username={}", username);
        boolean userExists = userRepository.existsByUsername(username);
        if (userExists) {
            log.warn("User registration failed; username={}, reason=username_already_exists", username);
            throw new DuplicateResourceException(CommonConstants.USER, "username", username);
        }

        String hashedPassword = passwordEncoder.encode(createUserRequest.getPassword());
        var user = createUserRequestToUser.apply(createUserRequest);
        user.setPassword(hashedPassword);

        var role = roleRepository.findByName(CommonConstants.ROLE_USER)
                .orElseThrow(() -> {
                    log.error("Seeding error for role={}; reason=role_not_seeded", CommonConstants.ROLE_USER);
                    return new IllegalStateException(DataSeeder.ROLE_USER_WAS_NOT_SEEDED);
                });

        user.addRole(role);

        var savedUser = userRepository.save(user);

        log.info("User registered; userId={}, username={}", savedUser.getId(), username);

        return userToUserResponse.apply(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User fetch failed; userId={}, reason=user_not_found", id);
                    return new ResourceNotFoundException(CommonConstants.USER, "id", String.valueOf(id));
                });

        return userToUserResponse.apply(user);
    }

    @Override
    @NullMarked
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(PrincipalUser::new)
                .orElseThrow(() -> UsernameNotFoundException.fromUsername(username));
    }
}
