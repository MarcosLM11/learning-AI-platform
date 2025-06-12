package com.marcos.studyasistant.usersservice.service.impl;

import com.marcos.studyasistant.usersservice.dto.UserRequestDto;
import com.marcos.studyasistant.usersservice.dto.UserResponseDto;
import com.marcos.studyasistant.usersservice.dto.UserUpdateDto;
import com.marcos.studyasistant.usersservice.entity.UserEntity;
import com.marcos.studyasistant.usersservice.mapper.UserMapper;
import com.marcos.studyasistant.usersservice.repository.UserRepository;
import com.marcos.studyasistant.usersservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        log.info("Creating new user {}", userRequestDto);

        // Convert DTO to Entity
        UserEntity user = userMapper.toEntity(userRequestDto);

        // Validate user entity
        validate(user);

        // Save the user entity to the database
        UserEntity savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        // Convert saved entity back to DTO
        return userMapper.toResponseDto(savedUser);
    }

    @Override
    public UserResponseDto getUserById(UUID id) {
        log.info("Fetching user with ID: {}", id);

        // Find user by ID or throw an exception if not found
        UserEntity userEntity = userRepository.findById(id).orElseThrow();
        log.info("User found: {}", userEntity);

        // Convert entity to DTO
        return userMapper.toResponseDto(userEntity);
    }

    @Override
    public UserResponseDto updateUser(UUID id, UserUpdateDto userUpdateDto) {
        log.info("Updating user with ID: {}", id);

        // Find user by ID or throw an exception if not found
        UserEntity userEntity = userRepository.findById(id).orElseThrow();

        // Update user entity fields
        userEntity = userMapper.updateEntity(userEntity, userUpdateDto);

        // Save the updated user entity to the database
        UserEntity updatedUser = userRepository.save(userEntity);
        log.info("User updated successfully with ID: {}", updatedUser.getId());

        // Convert updated entity back to DTO
        return userMapper.toResponseDto(updatedUser);
    }

    @Override
    public void deleteUser(UUID id) {
        log.info("Deleting user with ID: {}", id);

        // Find user by ID or throw an exception if not found
        UserEntity userEntity = userRepository.findById(id).orElseThrow();

        // Delete the user entity from the database
        userRepository.delete(userEntity);
        log.info("User deleted successfully with ID: {}", id);
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        log.info("Fetching all users");

        // Fetch all users from the repository
        List<UserEntity> userEntities = userRepository.findAll();
        log.info("{} users found", userEntities.size());

        // Convert list of entities to list of DTOs and return
        return userEntities.stream()
                .map(userMapper::toResponseDto)
                .toList();
    }

    @Override
    public void updatePassword(UUID id, String currentPassword, String newPassword) {
        log.debug("Changing password for user: {}", id);

        // Find user by ID or throw an exception if not found
        UserEntity userEntity = userRepository.findById(id).orElseThrow();

        // Validate current password
        if (!userEntity.getPassword().equals(currentPassword)) {
            log.error("Current password does not match for user: {}", userEntity.getUsername());
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Validate new password
        if (!isValidPassword(newPassword)) {
            log.error("New password is not strong enough for user: {}", userEntity.getUsername());
            throw new IllegalArgumentException("New password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character");
        }

        // Update password
        userEntity.setPassword(newPassword);
        log.info("Password updated successfully for user: {}", userEntity.getUsername());

        // Save the updated user entity to the database
        userRepository.save(userEntity);
    }

    private void validate(UserEntity userEntity) {
        if (!isValidPassword(userEntity.getPassword())) {
            log.error("Password is not stronger enough for user: {}", userEntity.getUsername());
            throw new IllegalArgumentException("Password must contain at least one lowercase letter, one uppercase letter, one digit, and one special character");
        }
    }

    private boolean isValidPassword(String password) {
        return  password.matches(".*[a-z].*") &&  // Al menos una minúscula
                password.matches(".*[A-Z].*") &&  // Al menos una mayúscula
                password.matches(".*\\d.*") &&    // Al menos un dígito
                password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*"); // Al menos un carácter especial
    }
}
