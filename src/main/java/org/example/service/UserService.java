package org.example.service;

import org.example.controller.dto.UserRequest;
import org.example.controller.dto.UserResponse;
import org.example.exception.ResourceNotFoundException;
import org.example.exception.UserAlreadyExistsException;
import org.example.mapper.UserMapper;
import org.example.repository.Entities.UserEntity;
import org.example.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, UserMapper userMapper,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(UserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }
        UserEntity userEntity = userMapper.toEntity(request);
        String hash = passwordEncoder.encode(request.password());
        userEntity.setPasswordHash(hash);
        userEntity.setRole(UserEntity.UsersRole.USER);
        return userMapper.toResponse(userRepository.save(userEntity));
    }

    @Transactional(readOnly = true)
    public ResponseEntity<UserResponse> getProfile(String username) {
        UserEntity user = userRepository.findByEmail(username).orElseThrow(() ->
            new ResourceNotFoundException("User does not exist"));
        return ResponseEntity.ok(userMapper.toResponse(user));
    }
}
