package com.library.services.impl;

import com.library.dtos.user.UserDTO;
import com.library.dtos.user.UserInsertDTO;
import com.library.dtos.user.UserUpdateDTO;
import com.library.models.entities.Role;
import com.library.models.entities.User;
import com.library.models.repositories.RoleRepository;
import com.library.models.repositories.UserRepository;
import com.library.projections.UserDetailsProjection;
import com.library.publisher.UserCreatedPublisher;
import com.library.services.IS3Service;
import com.library.services.exceptions.DatabaseException;
import com.library.services.exceptions.ResourceNotFoundException;
import com.library.factories.UserFactory;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl service;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private IS3Service s3Service;

    @Mock
    private UserRepository repository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserCreatedPublisher userCreatedPublisher;

    private long existingId;
    private long nonExistingId;
    private long dependentId;
    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        nonExistingId = 99L;
        dependentId = 4L;
        user = UserFactory.createUser();
        role = UserFactory.createRoleUser();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void findByIdShouldReturnUserDTOWhenIdExists() {
        when(repository.findById(existingId)).thenReturn(Optional.of(user));

        UserDTO result = service.findById(existingId);

        assertNotNull(result);
        assertEquals(existingId, result.getId());
    }

    @Test
    void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(nonExistingId));
    }

    @Test
    void findAllShouldReturnPageOfUserDTO() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(user));
        when(repository.findAll(pageable)).thenReturn(page);

        Page<UserDTO> result = service.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findByEmailShouldReturnPageOfUserDTO() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(user));
        when(repository.findByEmailIgnoreCaseContaining("felipe", pageable)).thenReturn(page);

        Page<UserDTO> result = service.findByEmail("felipe", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void insertShouldReturnUserDTOWhenValidWithoutPicture() {
        UserInsertDTO dto = UserFactory.createUserInsertDTO();
        dto.getRoles().add(new com.library.dtos.user.RoleDTO(role));

        when(roleRepository.getReferenceById(anyLong())).thenReturn(role);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("$2a$10$encoded");
        when(repository.save(any(User.class))).thenReturn(user);

        UserDTO result = service.insert(dto);

        assertNotNull(result);
        verify(passwordEncoder).encode(dto.getPassword());
        verify(repository, times(2)).save(any(User.class));
        verify(userCreatedPublisher).publishUserCreated(anyLong(), anyString(), anyString());
        verify(s3Service, never()).uploadFile(any(), anyString(), anyString());
    }

    @Test
    void insertShouldUploadPictureWhenProvided() {
        UserInsertDTO dto = UserFactory.createUserInsertDTO();
        dto.getRoles().add(new com.library.dtos.user.RoleDTO(role));
        MultipartFile picture = new MockMultipartFile(
                "profilePicture", "avatar.jpg", "image/jpeg", "data".getBytes());
        dto.setProfilePicture(picture);

        when(roleRepository.getReferenceById(anyLong())).thenReturn(role);
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("$2a$10$encoded");
        when(repository.save(any(User.class))).thenReturn(user);
        when(s3Service.uploadFile(any(), eq("users"), anyString()))
                .thenReturn("https://bucket.s3.amazonaws.com/users/1_profile.jpg");

        UserDTO result = service.insert(dto);

        assertNotNull(result);
        verify(s3Service).uploadFile(any(), eq("users"), anyString());
    }

    @Test
    void updateShouldReturnUserDTOWhenIdExists() {
        UserUpdateDTO dto = UserFactory.createUserUpdateDTO();
        dto.getRoles().add(new com.library.dtos.user.RoleDTO(role));

        when(repository.getReferenceById(existingId)).thenReturn(user);
        when(roleRepository.getReferenceById(anyLong())).thenReturn(role);
        when(repository.save(any(User.class))).thenReturn(user);

        UserDTO result = service.update(existingId, dto);

        assertNotNull(result);
        verify(repository).save(any(User.class));
    }

    @Test
    void updateShouldReplacePictureWhenProvidedAndOldExists() {
        UserUpdateDTO dto = UserFactory.createUserUpdateDTO();
        dto.getRoles().add(new com.library.dtos.user.RoleDTO(role));
        MultipartFile picture = new MockMultipartFile(
                "profilePicture", "new.jpg", "image/jpeg", "data".getBytes());
        dto.setProfilePicture(picture);

        User userWithAvatar = UserFactory.createUser();
        userWithAvatar.setAvatarUrl("https://bucket.s3.amazonaws.com/users/old.jpg");

        when(repository.getReferenceById(existingId)).thenReturn(userWithAvatar);
        when(roleRepository.getReferenceById(anyLong())).thenReturn(role);
        when(repository.save(any(User.class))).thenReturn(userWithAvatar);
        when(s3Service.uploadFile(any(), eq("users"), anyString()))
                .thenReturn("https://bucket.s3.amazonaws.com/users/1_profile.jpg");

        UserDTO result = service.update(existingId, dto);

        assertNotNull(result);
        verify(s3Service).deleteFile(anyString());
        verify(s3Service).uploadFile(any(), eq("users"), anyString());
    }

    @Test
    void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        UserUpdateDTO dto = UserFactory.createUserUpdateDTO();
        when(repository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);

        assertThrows(ResourceNotFoundException.class, () -> service.update(nonExistingId, dto));
    }

    @Test
    void deleteShouldDoNothingWhenIdExists() {
        when(repository.existsById(existingId)).thenReturn(true);
        doNothing().when(repository).deleteById(existingId);

        assertDoesNotThrow(() -> service.delete(existingId));
        verify(repository).deleteById(existingId);
    }

    @Test
    void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(repository.existsById(nonExistingId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.delete(nonExistingId));
    }

    @Test
    void deleteShouldThrowDatabaseExceptionWhenIntegrityViolation() {
        when(repository.existsById(dependentId)).thenReturn(true);
        doThrow(DataIntegrityViolationException.class).when(repository).deleteById(dependentId);

        assertThrows(DatabaseException.class, () -> service.delete(dependentId));
    }

    @Test
    void getMeShouldReturnAuthenticatedUserDTO() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("username")).thenReturn("felipe@biblioteca.com");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(jwt);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);

        when(repository.findByEmail("felipe@biblioteca.com")).thenReturn(Optional.of(user));

        UserDTO result = service.getMe();

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void loadUserByUsernameShouldReturnUserDetailsWhenUserExists() {
        UserDetailsProjection projection = new UserDetailsProjectionImpl(
                "felipe@biblioteca.com", "$2a$10$hash", 2L, "ROLE_USER");
        when(repository.searchUserAndRolesByEmail("felipe@biblioteca.com"))
                .thenReturn(List.of(projection));

        UserDetails result = service.loadUserByUsername("felipe@biblioteca.com");

        assertNotNull(result);
        assertEquals("felipe@biblioteca.com", result.getUsername());
    }

    @Test
    void loadUserByUsernameShouldThrowUsernameNotFoundExceptionWhenUserDoesNotExist() {
        when(repository.searchUserAndRolesByEmail("notfound@biblioteca.com"))
                .thenReturn(List.of());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("notfound@biblioteca.com"));
    }

    @Test
    void authenticatedShouldReturnUserWhenJwtValid() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("username")).thenReturn("felipe@biblioteca.com");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(jwt);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);

        when(repository.findByEmail("felipe@biblioteca.com")).thenReturn(Optional.of(user));

        User result = service.authenticated();

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    void authenticatedShouldThrowUsernameNotFoundExceptionWhenContextInvalid() {
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(context);

        assertThrows(UsernameNotFoundException.class, () -> service.authenticated());
    }

    static class UserDetailsProjectionImpl implements UserDetailsProjection {
        private final String username;
        private final String password;
        private final Long roleId;
        private final String authority;

        UserDetailsProjectionImpl(String username, String password, Long roleId, String authority) {
            this.username = username;
            this.password = password;
            this.roleId = roleId;
            this.authority = authority;
        }

        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public Long getRoleId() { return roleId; }
        public String getAuthority() { return authority; }
    }
}
