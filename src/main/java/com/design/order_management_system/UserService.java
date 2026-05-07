package com.design.order_management_system;

import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.converter.CreateUserRequestToUser;
import com.design.order_management_system.converter.UserToUserResponse;
import com.design.order_management_system.dto.CreateUserRequest;
import com.design.order_management_system.dto.UserResponse;
import com.design.order_management_system.model.Role;
import com.design.order_management_system.model.User;
import com.design.order_management_system.model.UserRoleMapping;
import com.design.order_management_system.model.UserRoleMappingKey;
import com.design.order_management_system.repository.RoleRepository;
import com.design.order_management_system.repository.UserRepository;
import com.design.order_management_system.repository.UserRoleMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserToUserResponse userToUserResponse;
    private final CreateUserRequestToUser createUserRequestToUser;
    private final UserRoleMappingRepository userRoleMappingRepository;
    
    public UserResponse createUser(CreateUserRequest createUserRequest) {
        User user = userRepository.save(createUserRequestToUser.apply(createUserRequest));
        Role role = roleRepository.save(Role.builder().name(CommonConstants.ROLE_USER).build());
        var userRoleMappingKey = UserRoleMappingKey.builder()
                .userId(user.getId())
                .roleId(role.getId())
                .build();
        var userRoleMapping = UserRoleMapping.builder()
                .id(userRoleMappingKey)
                .user(user)
                .role(role)
                .build();
        userRoleMappingRepository.save(userRoleMapping);
        return userToUserResponse.apply(user);
    }
    
    public UserResponse getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return userToUserResponse.apply(user);
    }
}
