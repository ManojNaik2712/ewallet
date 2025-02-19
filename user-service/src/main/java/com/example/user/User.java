package com.example.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.user.UserConstants.AUTHORITY_DELIMITER;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @Column(unique = true,nullable = false)
    private String phoneNumber;

    @Column(unique = true,nullable = false)
    private String email;

    private String password;

    private String authorities;

    @Enumerated(value = EnumType.STRING)
    private UserIdentifier userIdentifier;

    @Column(unique = true)
    private String identifierValue;

    @CreationTimestamp
    private Date createdOn;

    @UpdateTimestamp
    private Date UpdatedOn;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String[] authorities=this.authorities.split(AUTHORITY_DELIMITER);
        return Arrays.stream(authorities)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.phoneNumber;
    }
}
