package com.example.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;

import static com.example.user.UserConstants.*;
import static org.springframework.http.HttpMethod.POST;

@Configuration
public class UserSecurityConfig {
    @Autowired
    UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        httpSecurity.authorizeHttpRequests(auth -> auth
                .requestMatchers(POST,"/user").permitAll()
                .requestMatchers("/user/**").hasAuthority(USER_AUTHORITY)
                //.requestMatchers(POST,"/transact").permitAll()
                .requestMatchers(POST,"/admin").hasAnyAuthority(ADMIN_AUTHORITY,SERVICE_AUTHORITY)
                .requestMatchers("/delete").permitAll()
                .anyRequest().authenticated()
        ).httpBasic(httpBasic -> {
        }).csrf(CsrfConfigurer::disable)
                .formLogin(formLogin -> formLogin.defaultSuccessUrl("/home",true).permitAll())
                .logout(LogoutConfigurer::permitAll);
        return httpSecurity.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity httpSecurity) throws Exception{
        AuthenticationManagerBuilder authenticationManagerBuilder=
                httpSecurity.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService);
        return authenticationManagerBuilder.build();
    }



}
