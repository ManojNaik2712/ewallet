package com.example.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;

    @PostMapping
    public ResponseEntity createUser(@RequestBody UserCreateRequest userCreateRequest){
        userService.createUser(userCreateRequest);
        return new ResponseEntity("User created succesfully", HttpStatus.CREATED);

    }
    @GetMapping
    public User getUserDetails(){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        User user= (User) authentication.getPrincipal();
        return userService.loadUserByUsername(user.getUsername());
    }

}
