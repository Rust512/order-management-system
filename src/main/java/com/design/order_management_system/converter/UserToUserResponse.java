package com.design.order_management_system.converter;

import com.design.order_management_system.dto.UserResponse;
import com.design.order_management_system.model.User;
import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserToUserResponse implements Converter<User, UserResponse> {

    @Override
    public UserResponse convert(@NonNull User source) {
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
