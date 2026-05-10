package com.design.order_management_system.service;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.converter.CreateUserRequestToUser;
import com.design.order_management_system.converter.UserToUserResponse;
import com.design.order_management_system.dto.request.CreateUserRequest;
import com.design.order_management_system.dto.response.UserResponse;
import com.design.order_management_system.dto.security.PrincipalUser;
import com.design.order_management_system.exception.DuplicateResourceException;
import com.design.order_management_system.exception.ResourceNotFoundException;
import com.design.order_management_system.model.security.Role;
import com.design.order_management_system.model.security.User;
import com.design.order_management_system.repository.RoleRepository;
import com.design.order_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserToUserResponse userToUserResponse;
    private final CreateUserRequestToUser createUserRequestToUser;
    private final PasswordEncoder passwordEncoder;
    private final MapReactiveUserDetailsService mapReactiveUserDetailsService;

    @Transactional
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        boolean userExists = userRepository.existsByUsername(createUserRequest.getUsername());
        if (userExists) {
            throw new DuplicateResourceException(CommonConstants.USER, CommonConstants.USERNAME, createUserRequest.getUsername());
        }

        String hashedPassword = passwordEncoder.encode(createUserRequest.getPassword());
        var user = createUserRequestToUser.apply(createUserRequest);
        user.setPassword(hashedPassword);

        var role = roleRepository.findByName(CommonConstants.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(CommonConstants.ROLE_USER).build()));

        user.addRole(role);

        var savedUser = userRepository.save(user);

        return userToUserResponse.apply(savedUser);
    }

    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CommonConstants.USER, CommonConstants.ID, String.valueOf(id)));
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
