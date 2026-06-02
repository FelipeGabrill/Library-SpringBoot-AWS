package com.library.services.impl;

import com.library.dtos.user.RoleDTO;
import com.library.dtos.user.UserDTO;
import com.library.dtos.user.UserInsertDTO;
import com.library.dtos.user.UserUpdateDTO;
import com.library.models.entities.Role;
import com.library.models.entities.User;
import com.library.projections.UserDetailsProjection;
import com.library.publisher.UserCreatedPublisher;
import com.library.models.repositories.RoleRepository;
import com.library.models.repositories.UserRepository;
import com.library.services.IS3Service;
import com.library.services.IUserService;
import com.library.services.aws.S3Service;
import com.library.services.exceptions.DatabaseException;
import com.library.services.exceptions.ResourceNotFoundException;

import jakarta.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class UserServiceImpl implements IUserService, UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IS3Service s3Service;

    @Autowired
    private UserRepository repository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserCreatedPublisher userCreatedPublisher;

    @Override
    @Transactional(readOnly = true)
    public UserDTO findById(Long id) {
        return new UserDTO(findUserOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(UserDTO::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> findByEmail(String email, Pageable pageable) {
        return repository.findByEmailIgnoreCaseContaining(email, pageable).map(UserDTO::new);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getMe() {
        return new UserDTO(authenticated());
    }

    @Override
    @Transactional
    public UserDTO insert(UserInsertDTO dto) {
        log.info("Inserting user | email={}", dto.getEmail());

        User entity = new User();
        copyDtoToEntity(dto, entity);
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        entity = repository.save(entity);

        uploadProfilePictureIfPresent(dto.getProfilePicture(), entity);
        entity = repository.save(entity);

        publishUserCreatedEvent(entity);

        log.info("User created successfully | id={} | email={}", entity.getId(), entity.getEmail());
        return new UserDTO(entity);
    }

    @Override
    @Transactional
    public UserDTO update(Long id, UserUpdateDTO dto) {
        log.info("Updating user | id={}", id);
        try {
            User entity = repository.getReferenceById(id);
            copyDtoToEntity(dto, entity);

            replaceProfilePictureIfPresent(dto.getProfilePicture(), entity);

            entity = repository.save(entity);

            log.info("User updated successfully | id={}", id);
            return new UserDTO(entity);
        } catch (EntityNotFoundException e) {
            log.warn("User not found for update | id={}", id);
            throw new ResourceNotFoundException("Resource not found");
        }
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(Long id) {
        log.info("Deleting user | id={}", id);

        if (!repository.existsById(id)) {
            log.warn("User not found for delete | id={}", id);
            throw new ResourceNotFoundException("Resource not found");
        }
        try {
            repository.deleteById(id);
            log.info("User deleted successfully | id={}", id);
        } catch (DataIntegrityViolationException e) {
            log.error("Delete failed — referential integrity violation | id={}", id);
            throw new DatabaseException("Referential integrity violation");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<UserDetailsProjection> result = repository.searchUserAndRolesByEmail(username);

        if (result.isEmpty()) {
            log.warn("Authentication failed — user not found | email={}", username);
            throw new UsernameNotFoundException("User not found");
        }

        User user = buildUserFromProjection(username, result);

        log.info("User loaded successfully | email={}", username);
        return user;
    }

    @Override
    public User authenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Jwt jwtPrincipal = (Jwt) authentication.getPrincipal();
            String username = jwtPrincipal.getClaim("username");
            return repository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        } catch (Exception e) {
            log.warn("Failed to resolve authenticated user");
            throw new UsernameNotFoundException("User not found");
        }
    }

    private User buildUserFromProjection(String username, List<UserDetailsProjection> result) {
        User user = new User();
        user.setEmail(username);
        user.setPassword(result.get(0).getPassword());
        for (UserDetailsProjection projection : result) {
            user.addRole(new Role(projection.getRoleId(), projection.getAuthority()));
        }
        return user;
    }

    private void copyDtoToEntity(UserDTO dto, User entity) {
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());

        entity.getRoles().clear();
        for (RoleDTO roleDto : dto.getRoles()) {
            Role role = roleRepository.getReferenceById(roleDto.getId());
            entity.getRoles().add(role);
        }
    }

    private void uploadProfilePictureIfPresent(MultipartFile file, User user) {
        if (file == null || file.isEmpty()) return;

        String url = s3Service.uploadFile(
                file,
                "users",
                user.getId() + "_profile" + getExtension(file.getOriginalFilename())
        );
        user.setAvatarUrl(url);

        log.info("Profile picture uploaded | userId={} | url={}", user.getId(), url);
    }

    private void replaceProfilePictureIfPresent(MultipartFile file, User user) {
        if (file == null || file.isEmpty()) return;

        if (user.getAvatarUrl() != null) {
            s3Service.deleteFile(user.getAvatarUrl());
            log.info("Old profile picture deleted | userId={}", user.getId());
        }

        uploadProfilePictureIfPresent(file, user);
    }

    private void publishUserCreatedEvent(User user) {
        userCreatedPublisher.publishUserCreated(
                user.getId(),
                user.getEmail(),
                user.getName()
        );
        log.info("UserCreated event published to SNS | userId={} | email={}",
                user.getId(), user.getEmail());
    }

    private User findUserOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found | id={}", id);
                    return new ResourceNotFoundException("Resource not found");
                });
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf(".")).toLowerCase();
    }
}