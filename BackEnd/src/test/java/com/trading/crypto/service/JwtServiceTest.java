package com.trading.crypto.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private JwtService jwtService;

    private final String plainSecret = "mysecretkey123456789012345678901234";
    private final String encodedSecret = Base64.getEncoder().encodeToString(plainSecret.getBytes());

    @BeforeEach
    void setUp() throws IllegalAccessException, NoSuchFieldException {
        jwtService = new JwtService();
        Field secretField = JwtService.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtService, encodedSecret);
    }

    @Test
    void testGenerateAndExtractClaims() {
        String username = "testuser";
        Long userID = 42L;

        String token = jwtService.generateToken(username, userID);

        assertNotNull(token);
        assertEquals(username, jwtService.extractUsername(token));
        assertEquals(String.valueOf(userID), jwtService.extractUserID(token));
        assertFalse(jwtService.extractExpiration(token).before(new Date()));
    }

    @Test
    void testValidateToken_Valid() {
        String username = "user1";
        Long userID = 99L;

        String token = jwtService.generateToken(username, userID);

        UserDetails userDetails = new User(username, "password", Collections.emptyList());
        assertTrue(jwtService.validateToken(token, userDetails));
    }

    @Test
    void testValidateToken_InvalidUsername() {
        String token = jwtService.generateToken("gooduser", 1L);

        UserDetails userDetails = new User("baduser", "pass", Collections.emptyList());
        assertFalse(jwtService.validateToken(token, userDetails));
    }

    @Test
    void testExtractExpiration() {
        String token = jwtService.generateToken("someone", 123L);

        Date exp = jwtService.extractExpiration(token);
        assertTrue(exp.after(new Date()));
    }
}