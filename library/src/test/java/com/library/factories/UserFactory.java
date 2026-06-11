package com.library.factories;

import com.library.dtos.user.UserInsertDTO;
import com.library.dtos.user.UserUpdateDTO;
import com.library.models.entities.Role;
import com.library.models.entities.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class UserFactory {

    public static Role createRoleAdmin() {
        return new Role(1L, "ROLE_ADMIN");
    }

    public static Role createRoleUser() {
        return new Role(2L, "ROLE_USER");
    }

    public static User createUser() {
        Set<Role> roles = new HashSet<>();
        roles.add(createRoleUser());
        User user = new User(
                1L,
                "Felipe Gabriel",
                "felipe@biblioteca.com",
                "$2a$10$encodedPasswordHash",
                null,
                roles,
                new ArrayList<>(),
                new ArrayList<>()
        );
        return user;
    }

    public static User createUser(Long id, String email) {
        Set<Role> roles = new HashSet<>();
        roles.add(createRoleUser());
        return new User(
                id,
                "User " + id,
                email,
                "$2a$10$encodedPasswordHash",
                null,
                roles,
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    public static User createAdminUser() {
        Set<Role> roles = new HashSet<>();
        roles.add(createRoleAdmin());
        return new User(
                1L,
                "Admin",
                "admin@biblioteca.com",
                "$2a$10$encodedPasswordHash",
                null,
                roles,
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    public static UserInsertDTO createUserInsertDTO() {
        UserInsertDTO dto = new UserInsertDTO();
        dto.setName("New User");
        dto.setEmail("newuser@biblioteca.com");
        dto.setPassword("password123");
        return dto;
    }

    public static UserUpdateDTO createUserUpdateDTO() {
        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setName("Updated Name");
        dto.setEmail("updated@biblioteca.com");
        return dto;
    }
}

