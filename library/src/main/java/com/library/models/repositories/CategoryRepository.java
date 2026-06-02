package com.library.models.repositories;

import com.library.models.entities.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByNameIgnoreCase(String name);

    Page<Category> findByNameIgnoreCaseContaining(String name, Pageable pageable);

    @Query("SELECT obj FROM Category obj ORDER BY obj.name ASC")
    Page<Category> findAllPaged(Pageable pageable);
}
