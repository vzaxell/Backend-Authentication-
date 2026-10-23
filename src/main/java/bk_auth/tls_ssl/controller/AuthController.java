package bk_auth.tls_ssl.controller;

import bk_auth.tls_ssl.dto.RegisterRequest;
import bk_auth.tls_ssl.dto.UserResponse;
import bk_auth.tls_ssl.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /** Público: registra un usuario nuevo. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return UserResponse.from(userService.register(request));
    }

    /** Protegido (Basic Auth): devuelve el usuario autenticado. */
    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return UserResponse.from(userService.getByUsername(authentication.getName()));
    }
}
