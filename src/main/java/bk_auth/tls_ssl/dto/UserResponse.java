package bk_auth.tls_ssl.dto;

import bk_auth.tls_ssl.entity.AppUser;

public record UserResponse(Long id, String username, String email, String role) {
    public static UserResponse from(AppUser u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getRole());
    }
}
