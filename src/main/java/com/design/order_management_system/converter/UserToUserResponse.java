package com.design.order_management_system.converter;

import com.design.order_management_system.dto.response.UserResponse;
import com.design.order_management_system.model.security.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
public class UserToUserResponse implements Function<User, UserResponse> {

    @Override
    public UserResponse apply(User source) {
        List<String> roles = source.getRoles()
                .stream()
                .map(mapping -> mapping.getRole().getName())
                .toList();
        return UserResponse.builder()
                .username(source.getUsername())
                .roles(roles)
                .build();
    }
}
