package com.library.models.repositories;

import com.library.models.entities.User;
import com.library.projections.UserDetailsProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(nativeQuery = true, value = """
        SELECT tb_user.email AS login,
               tb_user.password AS password,
               tb_role.id AS roleId,
               tb_role.authority AS authority
        FROM tb_user
        INNER JOIN tb_user_role ON tb_user.id = tb_user_role.user_id
        INNER JOIN tb_role ON tb_role.id = tb_user_role.role_id
        WHERE tb_user.email = :email
        """)
    List<UserDetailsProjection> searchUserAndRolesByEmail(String email);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByEmailIgnoreCaseContaining(String email, Pageable pageable);
}
