package com.design.order_management_system.controller;

import com.design.order_management_system.service.UserService;
import com.design.order_management_system.dto.CreateUserRequest;
import com.design.order_management_system.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/v1/user")
public class UserController {
    private final UserService userService;

    @PostMapping
    UserResponse createUser(@RequestBody CreateUserRequest createUserRequest) {
        return userService.createUser(createUserRequest);
    }
    
    @GetMapping(path = "/{id}")
    UserResponse getUser(@PathVariable Long id) {
        return userService.getById(id);
    }
}
