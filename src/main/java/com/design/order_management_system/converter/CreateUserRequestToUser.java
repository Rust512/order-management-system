package com.design.order_management_system.converter;

import com.design.order_management_system.dto.request.CreateUserRequest;
import com.design.order_management_system.model.security.User;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class CreateUserRequestToUser implements Function<CreateUserRequest, User> {

    @Override
    public User apply(CreateUserRequest source) {
        // The password should be encrypted, the logic will be added later.
        return User.builder()
                .username(source.getUsername())
                .password(source.getPassword())
                .build();
    }
}
