package com.trading.crypto.controller;

import com.trading.crypto.dto.AppUser.AppUserCreateDto;
import com.trading.crypto.dto.AppUser.AppUserDto;
import com.trading.crypto.dto.AppUser.AppUserLoginDto;
import com.trading.crypto.repository.AppUserRepository;
import com.trading.crypto.service.AuthenticationService;
import com.trading.crypto.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {
    private AuthenticationService service;

    @Autowired
    public AuthController(AuthenticationService service){
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<AppUserDto> registerUser(@RequestBody AppUserCreateDto userCreateDto){
        AppUserDto userDto = this.service.createUser(userCreateDto);

        return new ResponseEntity<>(userDto,HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AppUserDto> loginUser(@RequestBody AppUserLoginDto userLoginDto){
        try{
            AppUserDto userDto = this.service.authenticateUser(userLoginDto);

            return new ResponseEntity<>(userDto, HttpStatus.OK);
        } catch (UsernameNotFoundException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}
