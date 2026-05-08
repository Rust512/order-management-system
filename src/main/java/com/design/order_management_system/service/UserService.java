package com.design.order_management_system.service;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.converter.CreateUserRequestToUser;
import com.design.order_management_system.converter.UserToUserResponse;
import com.design.order_management_system.dto.CreateUserRequest;
import com.design.order_management_system.dto.UserResponse;
import com.design.order_management_system.exception.DuplicateResourceException;
import com.design.order_management_system.exception.ResourceNotFoundException;
import com.design.order_management_system.model.Role;
import com.design.order_management_system.model.User;
import com.design.order_management_system.model.UserRoleMapping;
import com.design.order_management_system.repository.RoleRepository;
import com.design.order_management_system.repository.UserRepository;
import com.design.order_management_system.repository.UserRoleMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserToUserResponse userToUserResponse;
    private final CreateUserRequestToUser createUserRequestToUser;
    private final UserRoleMappingRepository userRoleMappingRepository;

    @Transactional
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        boolean userExists = userRepository.existsByUsername(createUserRequest.getUsername());
        if (userExists) {
            throw new DuplicateResourceException(CommonConstants.USER, CommonConstants.USERNAME, createUserRequest.getUsername());
        }
        userRepository.save(createUserRequestToUser.apply(createUserRequest));

        boolean roleExists = roleRepository.existsByName(CommonConstants.ROLE_USER);
        Role role;
        if (roleExists) {
            role = roleRepository.findByName(CommonConstants.ROLE_USER).orElse(null);
            assert role != null;
        } else {
            role = roleRepository.save(Role.builder().name(CommonConstants.ROLE_USER).build());
        }

        var user = userRepository.findByUsername(createUserRequest.getUsername()).orElse(null);
        assert user != null;
        var mapping = UserRoleMapping.builder()
                .user(user)
                .role(role)
                .build();
        user.getRoles().add(mapping);
        userRoleMappingRepository.save(mapping);

        return userToUserResponse.apply(user);
    }

    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CommonConstants.USER, CommonConstants.ID, String.valueOf(id)));
        return userToUserResponse.apply(user);
    }
}
