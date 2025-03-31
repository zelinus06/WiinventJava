package com.wiinventjava.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class ProfileUserResponse implements Serializable {
    private String username;
    private String avatarUrl;
    private int lotusPoint;
}
