package com.example.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.example.user.UserConstants.USER_AUTHORITY;

public class UserService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;
    public void createUser(UserCreateRequest userCreateRequest) {
        User user=userCreateRequest.toUser();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setAuthorities(USER_AUTHORITY);
        userRepository.save(user);
    }

    public User loadUserByUsername(String userName) throws UsernameNotFoundException {
        return userRepository.findByPhoneNumber(userName);
    }
}
