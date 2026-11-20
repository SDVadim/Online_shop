package org.example.service;

import org.example.repository.Entities.UserEntity;
import org.example.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        UserEntity user = userRepository.findByEmail(email.strip().toLowerCase(Locale.ROOT)).orElseThrow(() ->
            new UsernameNotFoundException("Неверная почта или пароль"));
        return User.withUsername(user.getEmail()).password(user.getPasswordHash()).roles(user.getRole().name()).build();
    }
}
