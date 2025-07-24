package com.trading.crypto.service;

import com.trading.crypto.model.AppUser;
import com.trading.crypto.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AppUserDetailsServiceTest {
    @Mock
    private AppUserRepository repo;

    @InjectMocks
    private AppUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadUserByUsernameSuccess() {
        AppUser appUser = new AppUser(1L, "user", "user@example.com", "hashedpass", new BigDecimal(1000));
        when(repo.findByUsername("user")).thenReturn(Optional.of(appUser));

        UserDetails details = userDetailsService.loadUserByUsername("user");

        assertEquals("user", details.getUsername());
        assertEquals("hashedpass", details.getPassword());
        assertTrue(details.getAuthorities().isEmpty());
    }

    @Test
    void testLoadUserByUsernameNotFound() {
        when(repo.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername("ghost"));
    }
}