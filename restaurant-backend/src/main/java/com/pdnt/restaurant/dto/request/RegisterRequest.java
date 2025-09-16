package com.pdnt.restaurant.dto.request;

import com.pdnt.restaurant.entity.enums.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private String email;
    private Role role;
    private String firstName;
    private String lastName;
}

