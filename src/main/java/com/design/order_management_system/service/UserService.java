package com.design.order_management_system.service;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.converter.CreateUserRequestToUser;
import com.design.order_management_system.converter.UserToUserResponse;
import com.design.order_management_system.dto.CreateUserRequest;
import com.design.order_management_system.dto.UserResponse;
import com.design.order_management_system.exception.DuplicateResourceException;
import com.design.order_management_system.exception.ResourceNotFoundException;
import com.design.order_management_system.model.security.Role;
import com.design.order_management_system.model.security.User;
import com.design.order_management_system.repository.RoleRepository;
import com.design.order_management_system.repository.UserRepository;
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

    @Transactional
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        boolean userExists = userRepository.existsByUsername(createUserRequest.getUsername());
        if (userExists) {
            throw new DuplicateResourceException(CommonConstants.USER, CommonConstants.USERNAME, createUserRequest.getUsername());
        }
        var user = userRepository.save(createUserRequestToUser.apply(createUserRequest));

        var role = roleRepository.findByName(CommonConstants.ROLE_USER)
                .orElse(roleRepository.save(Role.builder().name(CommonConstants.ROLE_USER).build()));

        user.addRole(role);

        return userToUserResponse.apply(user);
    }

    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CommonConstants.USER, CommonConstants.ID, String.valueOf(id)));
        return userToUserResponse.apply(user);
    }
}
