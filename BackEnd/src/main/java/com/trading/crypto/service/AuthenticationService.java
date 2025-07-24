package com.trading.crypto.service;

import com.trading.crypto.dto.AppUser.AppUserCreateDto;
import com.trading.crypto.dto.AppUser.AppUserDto;
import com.trading.crypto.dto.AppUser.AppUserLoginDto;
import com.trading.crypto.exception.UsernameAlreadyExistsException;
import com.trading.crypto.model.AppUser;
import com.trading.crypto.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AuthenticationService {
    private AppUserRepository repo;
    private PasswordEncoder encoder;
    private JwtService jwtService;
    private AuthenticationManager manager;

    @Autowired
    public AuthenticationService(AppUserRepository repo, PasswordEncoder encoder, JwtService jwtService, AuthenticationManager manager){
        this.repo = repo;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.manager = manager;
    }

    public AppUserDto createUser(AppUserCreateDto userCreateDto){
        if(repo.findByUsername(userCreateDto.getUsername()).isPresent()){
            throw new UsernameAlreadyExistsException("Username " + userCreateDto.getUsername() + " already exists.");
        }

        String hashedCode = encoder.encode(userCreateDto.getPassword());
        AppUser user = new AppUser(-1L, userCreateDto.getUsername(),userCreateDto.getEmail(), hashedCode, new BigDecimal(10000.00));
        user.setId(repo.save(user));

        return new AppUserDto(user.getId(), user.getUsername(), user.getEmail(), user.getBalance(), jwtService.generateToken(user.getUsername(), user.getId()));
    }

    public AppUserDto authenticateUser(AppUserLoginDto userLoginDto){
        Authentication authentication = manager.authenticate(new UsernamePasswordAuthenticationToken(userLoginDto.getUsername(), userLoginDto.getPassword()));

        if(!authentication.isAuthenticated()){
            throw new RuntimeException("Authentication failed for user: " + userLoginDto.getUsername());
        }

        AppUser user = repo.findByUsername(userLoginDto.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found after authentication. This should not happen."));
        String token = jwtService.generateToken(user.getUsername(), user.getId());

        return new AppUserDto(user.getId(), user.getUsername(), user.getEmail(), user.getBalance(), token);
    }
}
