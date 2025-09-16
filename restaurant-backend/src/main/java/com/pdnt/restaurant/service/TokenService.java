package com.pdnt.restaurant.service;

import com.pdnt.restaurant.dto.response.ApiResponse;
import com.pdnt.restaurant.entity.Token;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public interface TokenService {
    ApiResponse registerToken(String username);
    String authenticate(Map<String, String> request);
    Token findToken(String jwtToken);

    ApiResponse revokeToken(String username);
    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;
}
