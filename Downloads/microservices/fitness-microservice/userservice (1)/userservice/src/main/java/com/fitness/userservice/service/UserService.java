package com.fitness.userservice.service;

import com.fitness.userservice.UserRepository;
import com.fitness.userservice.dto.RegisterRequest;
import com.fitness.userservice.dto.UserResponse;
import lombok.AllArgsConstructor;
import com.fitness.userservice.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
@Slf4j
@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository repository;
    public UserResponse register(RegisterRequest request) {
        if(repository.existsByEmail(request.getEmail())){
            User existinguser= repository.findByEmail(request.getEmail());
            UserResponse userResponse=new UserResponse();
            userResponse.setId(existinguser.getId());
            userResponse.setEmail(existinguser.getEmail());
            userResponse.setFirstName(existinguser.getFirstName());
            userResponse.setLastName(existinguser.getLastName());
            userResponse.setPassword(existinguser.getPassword());
            userResponse.setCreatedAt(existinguser.getCreatedAt());
            userResponse.setUpdatedAt(existinguser.getUpdatedAt());
            return userResponse;
        }
        User user=new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setKeycloakId(request.getKeycloakId());
        user.setLastName(request.getLastName());
        user.setPassword(request.getPassword());

        User saveduser= repository.save(user);
        UserResponse userResponse=new UserResponse();
        userResponse.setId(saveduser.getId());
        userResponse.setEmail(saveduser.getEmail());
        userResponse.setKeycloakId(saveduser.getKeycloakId());
        userResponse.setFirstName(saveduser.getFirstName());
        userResponse.setLastName(saveduser.getLastName());
        userResponse.setPassword(saveduser.getPassword());
        userResponse.setCreatedAt(saveduser.getCreatedAt());
        userResponse.setUpdatedAt(saveduser.getUpdatedAt());
        return userResponse;
    }

    public UserResponse getUserProfile(String userId) {
        User user=repository.findById(userId)
                .orElseThrow(() ->new RuntimeException("User not found"));
        UserResponse userResponse=new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setEmail(user.getEmail());
        userResponse.setFirstName(user.getFirstName());
        userResponse.setLastName(user.getLastName());
        userResponse.setPassword(user.getPassword());
        userResponse.setCreatedAt(user.getCreatedAt());
        userResponse.setUpdatedAt(user.getUpdatedAt());
        return userResponse;
    }

    public Boolean existByUserId(String userId) {
        log.info("Calling User Service for {}",userId);
        return repository.existsByKeycloakId(userId);
    }
}
