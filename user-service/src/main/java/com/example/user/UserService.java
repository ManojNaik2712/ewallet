package com.example.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.user.UserConstants.USER_AUTHORITY;
import static com.example.utils.CommonConstants.*;

@Service
public class UserService{
    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JWTService jwtService;

    @Autowired
    AuthenticationManager authenticationManager;

    public void createUser(UserCreateRequest userCreateRequest) throws JsonProcessingException {
        User user=userCreateRequest.toUser();
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setAuthorities(USER_AUTHORITY);
        userRepository.save(user);


        JSONObject jsonObject=new JSONObject();
        jsonObject.put(USER_CREATED_TOPIC_USER_ID,user.getId());
        jsonObject.put(USER_CREATED_TOPIC_PHONE_NUMBER,user.getPhoneNumber());
        jsonObject.put(USER_CREATED_TOPIC_EMAIL, user.getEmail());
        jsonObject.put(USER_CREATED_TOPIC_IDENTIFIER_KEY, user.getUserIdentifier());
        jsonObject.put(USER_CREATED_TOPIC_IDENTIFIER_VALUE, user.getIdentifierValue());

        kafkaTemplate.send(USER_CREATED_TOPIC,objectMapper.writeValueAsString(jsonObject));
    }



    public List<User> getAllUserDetails() {
        return userRepository.findAll();
    }

    public String deleteAll() {
        userRepository.deleteAll();
        return "deleted succesfully";
    }

    public String verify(LoginDTO loginDTO) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getPhoneNumber(), loginDTO.getPassword())
            );
            if (authentication.isAuthenticated()) {
                String token = jwtService.generateToken(loginDTO);
                return token;
            } else {
                System.out.println("Authentication failed!");
                return "fail";
            }
        } catch (Exception e) {
            System.out.println("Authentication error: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    public User getUserDetail(String username) {
        return userRepository.findByName(username);
    }
}
