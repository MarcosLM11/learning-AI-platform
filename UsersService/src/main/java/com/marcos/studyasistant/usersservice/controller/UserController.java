package com.marcos.studyasistant.usersservice.controller;

import com.marcos.studyasistant.usersservice.dto.UserPasswordUpdateDto;
import com.marcos.studyasistant.usersservice.dto.UserRequestDto;
import com.marcos.studyasistant.usersservice.dto.UserResponseDto;
import com.marcos.studyasistant.usersservice.dto.UserUpdateDto;
import com.marcos.studyasistant.usersservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // CRUD operations for User entity
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@RequestBody UserRequestDto userRequestDto) {
        UserResponseDto userResponseDto = userService.createUser(userRequestDto);
        return ResponseEntity.ok(userResponseDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable UUID id) {
        UserResponseDto userResponseDto = userService.getUserById(id);
        return ResponseEntity.ok(userResponseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable UUID id, @RequestBody UserUpdateDto userUpdateDto) {
        UserResponseDto userResponseDto = userService.updateUser(id, userUpdateDto);
        return ResponseEntity.ok(userResponseDto);
    }

    public ResponseEntity updatePassword(@PathVariable UUID id, @RequestBody UserPasswordUpdateDto userPasswordUpdateDto) {
        userService.updatePassword(id, userPasswordUpdateDto.currentPassword() ,userPasswordUpdateDto.newPassword());
        return ResponseEntity.ok("Password updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    //Additional methods for user management

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}
