package com.marcos.studyasistant.usersservice.mapper;

import com.marcos.studyasistant.usersservice.dto.UserRequestDto;
import com.marcos.studyasistant.usersservice.dto.UserResponseDto;
import com.marcos.studyasistant.usersservice.dto.UserUpdateDto;
import com.marcos.studyasistant.usersservice.entity.UserEntity;
import com.marcos.studyasistant.usersservice.entity.enums.Role;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserEntity toEntity(UserRequestDto userRequestDto) {
        return new UserEntity().builder()
                .username(userRequestDto.username())
                .password(userRequestDto.password())
                .email(userRequestDto.email())
                .name(userRequestDto.name())
                .surname(userRequestDto.surname())
                .phone(userRequestDto.phone())
                .role(Role.valueOf(userRequestDto.role()))
                .build();
    }

    public UserResponseDto toResponseDto(UserEntity userEntity) {
        return new UserResponseDto(
                userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getPassword(),
                userEntity.getEmail(),
                userEntity.getName(),
                userEntity.getSurname(),
                userEntity.getPhone(),
                userEntity.getRole().getAuthority()
        );
    }

    public UserEntity updateEntity(UserEntity userEntity, UserUpdateDto userRequestDto) {
        userEntity.setUsername(userRequestDto.username());
        userEntity.setEmail(userRequestDto.email());
        userEntity.setName(userRequestDto.name());
        userEntity.setSurname(userRequestDto.surname());
        userEntity.setPhone(userRequestDto.phone());
        return userEntity;
    }
}
