package com.trading.crypto.service;

import com.trading.crypto.dto.AppUser.AppUserCreateDto;
import com.trading.crypto.dto.AppUser.AppUserDto;
import com.trading.crypto.dto.AppUser.AppUserLoginDto;
import com.trading.crypto.exception.UsernameAlreadyExistsException;
import com.trading.crypto.model.AppUser;
import com.trading.crypto.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    @Mock
    private AppUserRepository repo;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void testCreateUserSuccess() {
        AppUserCreateDto dto = new AppUserCreateDto();
        dto.setUsername("testusername");
        dto.setEmail("test@example.com");
        dto.setPassword("password");

        when(repo.findByUsername("testusername")).thenReturn(Optional.empty());
        when(encoder.encode("password")).thenReturn("hashed");
        when(repo.save(any(AppUser.class))).thenReturn(1L);
        when(jwtService.generateToken("testusername", 1L)).thenReturn("jwt-token");

        AppUserDto result = authenticationService.createUser(dto);

        assertEquals("testusername", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(new BigDecimal(10000.00), result.getBalance());
        assertEquals("jwt-token", result.getToken());
    }

    @Test
    void testCreateUserUsernameExists() {
        AppUserCreateDto dto = new AppUserCreateDto();
        dto.setUsername("existing");
        when(repo.findByUsername("existing")).thenReturn(Optional.of(new AppUser()));

        assertThrows(UsernameAlreadyExistsException.class, () -> authenticationService.createUser(dto));
    }

    @Test
    void testAuthenticateUserSuccess() {
        AppUserLoginDto loginDto = new AppUserLoginDto();
        loginDto.setUsername("testusername");
        loginDto.setPassword("password");

        Authentication mockAuth = mock(Authentication.class);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuth);
        when(mockAuth.isAuthenticated()).thenReturn(true);

        AppUser user = new AppUser(1L, "testusername", "email@test.com", "hashed", new BigDecimal(9000));
        when(repo.findByUsername("testusername")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("testusername", 1L)).thenReturn("jwt-token");

        AppUserDto result = authenticationService.authenticateUser(loginDto);

        assertEquals("testusername", result.getUsername());
        assertEquals("jwt-token", result.getToken());
    }

    @Test
    void testAuthenticateUserFailsAuthentication() {
        AppUserLoginDto loginDto = new AppUserLoginDto();
        loginDto.setUsername("badusername");
        loginDto.setPassword("wrongpassword");

        Authentication mockAuth = mock(Authentication.class);

        when(authManager.authenticate(any())).thenReturn(mockAuth);
        when(mockAuth.isAuthenticated()).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authenticationService.authenticateUser(loginDto));
    }

    @Test
    void testAuthenticateUserUserNotFound() {
        AppUserLoginDto loginDto = new AppUserLoginDto();
        loginDto.setUsername("ghost");
        loginDto.setPassword("pass");

        Authentication mockAuth = mock(Authentication.class);
        when(authManager.authenticate(any())).thenReturn(mockAuth);
        when(mockAuth.isAuthenticated()).thenReturn(true);
        when(repo.findByUsername("ghost")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authenticationService.authenticateUser(loginDto));

        assertTrue(exception.getMessage().contains("User not found"));
    }
}