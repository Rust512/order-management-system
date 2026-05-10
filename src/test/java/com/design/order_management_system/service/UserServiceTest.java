package com.design.order_management_system.service;

import com.design.order_management_system.config.seeder.RoleSeeder;
import com.design.order_management_system.constants.CommonConstants;
import com.design.order_management_system.converter.CreateUserRequestToUser;
import com.design.order_management_system.converter.UserToUserResponse;
import com.design.order_management_system.dto.request.CreateUserRequest;
import com.design.order_management_system.dto.response.UserResponse;
import com.design.order_management_system.exception.DuplicateResourceException;
import com.design.order_management_system.model.security.Role;
import com.design.order_management_system.model.security.User;
import com.design.order_management_system.repository.RoleRepository;
import com.design.order_management_system.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserToUserResponse userToUserResponse;
    @Mock
    private CreateUserRequestToUser createUserRequestToUser;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName(value = """
            When a user with the given username already exists,
            the method should throw a DuplicateResourceException.
            """)
    void createUser_WhenUsernameExists_ShouldThrowException() {
        String username = "U0";
        var createUserRequest = new CreateUserRequest(username, null);
        when(userRepository.existsByUsername(username)).thenReturn(true);
        Assertions.assertThatThrownBy(() -> userService.createUser(createUserRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining(username);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder, createUserRequestToUser, roleRepository, userToUserResponse);
    }

    @Test
    @DisplayName(value = """
            When a user with the given username does not exist, and USER_ROLE exists,
            the method should return a UserResponse object without saving any role.
            """)
    void createUser_WhenUsernameDoesNotExistAndRoleExists_ShouldReturnUserResponseWithoutSavingRole() {
        String username = "U0";
        String password = "P0";
        String hashedPassword = "HP0";
        var createUserRequest = new CreateUserRequest(username, password);
        var unsavedUser = User.builder()
                .username(username)
                .password(password)
                .build();
        var savedUser = User.builder()
                .id(1L)
                .username(username)
                .password(password)
                .build();
        var role = Role.builder()
                .id(1L)
                .name(CommonConstants.ROLE_USER)
                .build();
        var response = UserResponse.builder()
                .username(username)
                .roles(List.of(CommonConstants.ROLE_USER))
                .build();

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(hashedPassword);
        when(createUserRequestToUser.apply(createUserRequest)).thenReturn(unsavedUser);
        when(roleRepository.findByName(CommonConstants.ROLE_USER)).thenReturn(Optional.of(role));
        when(userRepository.save(unsavedUser)).thenReturn(savedUser);
        when(userToUserResponse.apply(savedUser)).thenReturn(response);

        var actualResponse = userService.createUser(createUserRequest);

        Assertions.assertThat(actualResponse)
                .returns(username, UserResponse::getUsername);
        Assertions.assertThat(actualResponse.getRoles())
                .containsExactly(CommonConstants.ROLE_USER);

        verify(userRepository).save(unsavedUser);
    }

    @Test
    @DisplayName(value = """
            When a user with the given username does not exist, and USER_ROLE does not exist,
            the method should throw an IllegalStateException.
            """)
    void createUser_WhenUsernameDoesNotExistAndRoleDoesNotExist_ShouldThrowException() {
        String username = "U0";
        String password = "P0";
        String hashedPassword = "HP0";
        var createUserRequest = new CreateUserRequest(username, password);
        var unsavedUser = User.builder()
                .username(username)
                .password(password)
                .build();

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(hashedPassword);
        when(createUserRequestToUser.apply(createUserRequest)).thenReturn(unsavedUser);
        when(roleRepository.findByName(CommonConstants.ROLE_USER)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> userService.createUser(createUserRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(RoleSeeder.ROLE_USER_WAS_NOT_SEEDED);

        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(userToUserResponse);
    }
}