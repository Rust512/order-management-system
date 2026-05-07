package com.design.order_management_system.converter;

import com.design.order_management_system.dto.CreateUserRequest;
import com.design.order_management_system.model.User;
import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CreateUserRequestToUser implements Converter<CreateUserRequest, User> {

    @Override
    public User convert(@NonNull CreateUserRequest source) {
        // The password should be encrypted, the logic will be added later.
        return User.builder()
                .username(source.getUsername())
                .password(source.getPassword())
                .build();
    }
}
