package com.wiinventjava.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileUserResponse {
    private String username;
    private String avatarUrl;
    private int lotusPoint;
}
