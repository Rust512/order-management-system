package com.design.order_management_system.converter;

import com.design.order_management_system.dto.request.CreateUserRequest;
import com.design.order_management_system.model.security.User;
import java.util.function.Function;
import org.springframework.stereotype.Component;

@Component
public class CreateUserRequestToUser implements Function<CreateUserRequest, User> {
    @Override
    public User apply(CreateUserRequest source) {
        return User.builder().username(source.getUsername()).password(source.getPassword()).build();
    }
}
