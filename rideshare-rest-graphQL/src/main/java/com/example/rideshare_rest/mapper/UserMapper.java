package com.example.rideshare_rest.mapper;

import com.example.rideshare_api_contract.dto.*;
import com.example.rideshare_rest.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toResponse(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        return UserResponse.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .birthDate(entity.getBirthDate())
                .build();
    }

    public UserEntity toEntity(UserRequest request) {
        if (request == null) {
            return null;
        }

        return UserEntity.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .birthDate(request.birthDate())
                .build();
    }

    public void updateEntityFromRequest(UserRequest request, UserEntity entity) {
        if (request == null || entity == null) {
            return;
        }

        entity.setFirstName(request.firstName());
        entity.setLastName(request.lastName());
        entity.setEmail(request.email());
        entity.setBirthDate(request.birthDate());
    }

    public void updateEntityFromPatch(PatchUserRequest request, UserEntity entity) {
        if (request == null || entity == null) {
            return;
        }

        if (request.firstName() != null) {
            entity.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            entity.setLastName(request.lastName());
        }
        if (request.email() != null) {
            entity.setEmail(request.email());
        }
        if (request.birthDate() != null) {
            entity.setBirthDate(request.birthDate());
        }
    }
}