package com.promisetracker.authentication.dto;

import lombok.Data;

@Data
public class RefreshRequest {

    /** Optional when refresh token is supplied via HttpOnly cookie. */
    private String refreshToken;
}
