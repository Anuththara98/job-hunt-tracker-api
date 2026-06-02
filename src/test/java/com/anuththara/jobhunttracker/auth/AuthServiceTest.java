package com.anuththara.jobhunttracker.auth;

import com.anuththara.jobhunttracker.auth.dto.AuthResponse;
import com.anuththara.jobhunttracker.auth.dto.LoginRequest;
import com.anuththara.jobhunttracker.auth.dto.RegisterRequest;
import com.anuththara.jobhunttracker.security.JwtService;
import com.anuththara.jobhunttracker.user.Role;
import com.anuththara.jobhunttracker.user.User;
import com.anuththara.jobhunttracker.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthService authService;

    @Test
    void register_validRequest_returnsAuthResponseWithToken() {
        RegisterRequest request = new RegisterRequest("Anuththara", "Kavindi", "anu@test.com", "password123");

        when(userRepository.findByEmail("anu@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("mock-jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("mock-jwt-token");
        assertThat(response.email()).isEqualTo("anu@test.com");
        assertThat(response.firstName()).isEqualTo("Anuththara");
        assertThat(response.lastName()).isEqualTo("Kavindi");
        verify(userRepository).save(argThat(u ->
                u.getEmail().equals("anu@test.com") &&
                u.getPassword().equals("hashed-password") &&
                u.getRole() == Role.USER
        ));
    }

    @Test
    void register_duplicateEmail_throwsEmailAlreadyExistsException() {
        RegisterRequest request = new RegisterRequest("Jane", "Doe", "existing@test.com", "password123");
        when(userRepository.findByEmail("existing@test.com"))
                .thenReturn(Optional.of(User.builder().email("existing@test.com").build()));

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_validCredentials_returnsAuthResponse() {
        LoginRequest request = new LoginRequest("anu@test.com", "password123");
        User user = User.builder()
                .email("anu@test.com").firstName("Anuththara").lastName("Kavindi")
                .password("hashed").role(Role.USER).build();

        when(userRepository.findByEmail("anu@test.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("mock-jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("mock-jwt-token");
        assertThat(response.email()).isEqualTo("anu@test.com");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_wrongPassword_throwsBadCredentialsException() {
        LoginRequest request = new LoginRequest("anu@test.com", "wrongpassword");
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(userRepository, never()).findByEmail(any());
    }
}