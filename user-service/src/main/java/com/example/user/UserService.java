package com.example.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.user.UserConstants.USER_AUTHORITY;
import static com.example.utils.CommonConstants.*;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

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

    public User loadUserByUsername(String userName) throws UsernameNotFoundException {
        return userRepository.findByPhoneNumber(userName);
    }


    public List<User> getAllUserDetails() {
        return userRepository.findAll();
    }

    public String deleteAll() {
        userRepository.deleteAll();
        return "deleted succesfully";
    }
}
