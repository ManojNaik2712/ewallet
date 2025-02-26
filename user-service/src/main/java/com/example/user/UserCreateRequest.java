package com.example.user;

import com.example.utils.UserIdentifier;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private UserIdentifier userIdentifier;

    @NotBlank
    private String identifierValue;

    User toUser(){
        return User.builder()
                .name(name).password(password)
                .phoneNumber(phoneNumber).email(email)
                .userIdentifier(userIdentifier).identifierValue(identifierValue)
                .build();

    }


}
