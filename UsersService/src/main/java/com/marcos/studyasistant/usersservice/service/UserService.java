package com.marcos.studyasistant.usersservice.service;

import com.marcos.studyasistant.usersservice.dto.UserRequestDto;
import com.marcos.studyasistant.usersservice.dto.UserResponseDto;
import com.marcos.studyasistant.usersservice.dto.UserUpdateDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public interface UserService {

    /**
     * Creates a new user.
     *
     * @param userRequestDto the user request data transfer object
     * @return the created user response data transfer object
     */
    UserResponseDto createUser(@Valid UserRequestDto userRequestDto);

    /**
     * Retrieves a user by their ID.
     *
     * @param id the UUID of the user
     * @return the user response data transfer object
     */
    UserResponseDto getUserById(UUID id);

    /**
     * Updates an existing user.
     *
     * @param id the UUID of the user to update
     * @param userUpdateDto the user update data transfer object
     * @return the updated user response data transfer object
     */
    UserResponseDto updateUser(UUID id, @Valid UserUpdateDto userUpdateDto);

    /**
     * Deletes a user by their ID.
     *
     * @param id the UUID of the user to delete
     */
    void deleteUser(UUID id);

    /**
     * Retrieves all users.
     *
     * @return a list of user response data transfer objects
     */
    List<UserResponseDto> getAllUsers();

    /**
     * Updates the password of a user.
     *
     * @param id the UUID of the user whose password is to be updated
     * @param newPassword the new password for the user
     */
    void updatePassword(UUID id, String currentPassword, String newPassword);
}
