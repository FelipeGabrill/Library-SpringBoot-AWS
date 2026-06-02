package com.library.services.impl;

import com.library.dtos.book.BookRequestDTO;
import com.library.dtos.book.BookResponseDTO;
import com.library.models.entities.Book;
import com.library.models.entities.Category;
import com.library.models.entities.Media;
import com.library.models.repositories.BookRepository;
import com.library.models.repositories.CategoryRepository;
import com.library.services.IBookService;
import com.library.services.IS3Service;
import com.library.services.aws.S3Service;
import com.library.services.exceptions.BusinessException;
import com.library.services.exceptions.DatabaseException;
import com.library.services.exceptions.ResourceNotFoundException;

import jakarta.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
public class BookServiceImpl implements IBookService {

    private static final Logger log = LoggerFactory.getLogger(BookServiceImpl.class);

    @Autowired
    private BookRepository repository;

    @Autowired
    private IS3Service s3Service;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public BookResponseDTO findById(Long id) {
        Book entity = repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Book not found | id={}", id);
                    return new ResourceNotFoundException("Resource not found");
                });
        return new BookResponseDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponseDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(BookResponseDTO::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponseDTO> search(String query, Pageable pageable) {
        log.info("Searching books | query={}", query);
        return repository.searchByTitleOrAuthor(query, pageable).map(BookResponseDTO::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponseDTO> findByCategory(Long categoryId, Pageable pageable) {
        return repository.findByCategoryId(categoryId, pageable).map(BookResponseDTO::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookResponseDTO> findAvailable(Pageable pageable) {
        return repository.findByAvailableCopiesGreaterThan(0, pageable).map(BookResponseDTO::new);
    }

    @Override
    @Transactional
    public BookResponseDTO insert(BookRequestDTO dto) {
        log.info("Inserting book | title={} | isbn={}", dto.getTitle(), dto.getIsbn());

        checkDuplicateIsbn(dto.getIsbn(), null);
        Book entity = new Book();
        copyDtoToEntity(dto, entity);
        entity = repository.save(entity);
        saveBookImages(dto.getMedia(), entity);
        entity = repository.save(entity);

        log.info("Book created successfully | id={} | title={}", entity.getId(), entity.getTitle());
        return new BookResponseDTO(entity);
    }

    @Override
    @Transactional
    public BookResponseDTO update(Long id, BookRequestDTO dto) {
        log.info("Updating book | id={} | title={}", id, dto.getTitle());
        try {
            checkDuplicateIsbn(dto.getIsbn(), id);
            Book entity = repository.getReferenceById(id);
            copyDtoToEntity(dto, entity);

            if (dto.getMedia() != null && !dto.getMedia().isEmpty()) {
                log.info("Replacing media | bookId={} | oldCount={} | newCount={}",
                        id, entity.getMedia().size(), dto.getMedia().size());
                for (Media media : entity.getMedia()) {
                    s3Service.deleteFile(media.getMediaUrl());
                }
                entity.getMedia().clear();
            }

            saveBookImages(dto.getMedia(), entity);
            entity = repository.save(entity);

            log.info("Book updated successfully | id={}", id);
            return new BookResponseDTO(entity);
        } catch (EntityNotFoundException e) {
            log.warn("Book not found for update | id={}", id);
            throw new ResourceNotFoundException("Resource not found");
        }
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(Long id) {
        log.info("Deleting book | id={}", id);
        if (!repository.existsById(id)) {
            log.warn("Book not found for delete | id={}", id);
            throw new ResourceNotFoundException("Resource not found");
        }
        try {
            repository.deleteById(id);
            log.info("Book deleted successfully | id={}", id);
        } catch (DataIntegrityViolationException e) {
            log.error("Delete failed — referential integrity violation | id={}", id);
            throw new DatabaseException("Referential integrity violation");
        }
    }

    @Override
    public Book getBook(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Book not found | id={}", id);
                    return new ResourceNotFoundException("Resource not found");
                });
    }

    private void checkDuplicateIsbn(String isbn, Long currentId) {
        if (isbn == null || isbn.isBlank()) return;
        repository.findByIsbn(isbn).ifPresent(existing -> {
            if (!Objects.equals(existing.getId(), currentId)) {
                log.warn("Duplicate ISBN detected | isbn={} | existingBookId={}",
                        isbn, existing.getId());
                throw new BusinessException("ISBN already exists: " + isbn);
            }
        });
    }

    private void copyDtoToEntity(BookRequestDTO dto, Book entity) {
        validatePublicationYear(dto.getPublicationYear());
        Category category = validateCategory(dto.getCategoryId());

        entity.setTitle(dto.getTitle());
        entity.setAuthor(dto.getAuthor());
        entity.setPublisher(dto.getPublisher());
        entity.setPublicationYear(dto.getPublicationYear());
        entity.setIsbn(dto.getIsbn());
        entity.setCategory(category);

        if (entity.getId() == null) {
            entity.setTotalCopies(dto.getTotalCopies());
            entity.setAvailableCopies(dto.getTotalCopies());
        } else {
            int diff = dto.getTotalCopies() - entity.getTotalCopies();
            entity.setTotalCopies(dto.getTotalCopies());
            entity.setAvailableCopies(entity.getAvailableCopies() + diff);
        }
    }

    private void validatePublicationYear(Integer year) {
        if (year == null) {
            throw new BusinessException("Publication year is required");
        }
        int currentYear = java.time.Year.now().getValue();
        if (year < 1000 || year > currentYear) {
            throw new BusinessException(
                    "Publication year must be between 1000 and " + currentYear);
        }
    }

    private Category validateCategory(Long categoryId) {
        if (categoryId == null) {
            throw new BusinessException("Category is required");
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("Category not found | categoryId={}", categoryId);
                    return new ResourceNotFoundException("Category not found: " + categoryId);
                });
    }

    private void saveBookImages(List<MultipartFile> medias, Book book) {
        if (medias == null || medias.isEmpty()) {
            return;
        }
        log.info("Uploading {} image(s) to S3 | bookId={}", medias.size(), book.getId());
        List<String> mediaUrls = s3Service.uploadFiles(medias, "books", book.getId());
        for (int i = 0; i < mediaUrls.size(); i++) {
            book.getMedia().add(new Media(mediaUrls.get(i), i + 1));
        }
        log.info("Images uploaded successfully | bookId={} | count={}", book.getId(), mediaUrls.size());
    }
}