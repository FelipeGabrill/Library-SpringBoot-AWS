package com.library.services.impl;

import com.library.dtos.book.BookRequestDTO;
import com.library.dtos.book.BookResponseDTO;
import com.library.models.entities.Book;
import com.library.models.entities.Category;
import com.library.models.entities.Media;
import com.library.models.repositories.BookRepository;
import com.library.models.repositories.CategoryRepository;
import com.library.services.IS3Service;
import com.library.services.exceptions.BusinessException;
import com.library.services.exceptions.DatabaseException;
import com.library.services.exceptions.ResourceNotFoundException;
import com.library.factories.BookFactory;
import com.library.factories.CategoryFactory;

import jakarta.persistence.EntityNotFoundException;

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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @InjectMocks
    private BookServiceImpl service;

    @Mock
    private BookRepository repository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private IS3Service s3Service;

    private long existingId;
    private long nonExistingId;
    private long dependentId;
    private Book book;
    private Category category;
    private BookRequestDTO bookRequestDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        nonExistingId = 99L;
        dependentId = 4L;
        book = BookFactory.createBook();
        category = CategoryFactory.createCategory();
        bookRequestDTO = BookFactory.createBookRequestDTO();
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void findByIdShouldReturnBookResponseDTOWhenIdExists() {
        when(repository.findById(existingId)).thenReturn(Optional.of(book));

        BookResponseDTO result = service.findById(existingId);

        assertNotNull(result);
        assertEquals(existingId, result.getId());
        assertEquals(book.getTitle(), result.getTitle());
    }

    @Test
    void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(nonExistingId));
    }

    @Test
    void findAllShouldReturnPageOfBookResponseDTO() {
        Page<Book> page = new PageImpl<>(List.of(book));
        when(repository.findAll(pageable)).thenReturn(page);

        Page<BookResponseDTO> result = service.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void searchShouldReturnPageOfBookResponseDTO() {
        Page<Book> page = new PageImpl<>(List.of(book));
        when(repository.searchByTitleOrAuthor("clean", pageable)).thenReturn(page);

        Page<BookResponseDTO> result = service.search("clean", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findByCategoryShouldReturnPageOfBookResponseDTO() {
        Page<Book> page = new PageImpl<>(List.of(book));
        when(repository.findByCategoryId(1L, pageable)).thenReturn(page);

        Page<BookResponseDTO> result = service.findByCategory(1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findAvailableShouldReturnPageOfBookResponseDTO() {
        Page<Book> page = new PageImpl<>(List.of(book));
        when(repository.findByAvailableCopiesGreaterThan(0, pageable)).thenReturn(page);

        Page<BookResponseDTO> result = service.findAvailable(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void insertShouldReturnBookResponseDTOWhenValidWithoutMedia() {
        when(repository.findByIsbn(bookRequestDTO.getIsbn())).thenReturn(Optional.empty());
        when(categoryRepository.findById(bookRequestDTO.getCategoryId())).thenReturn(Optional.of(category));
        when(repository.save(any(Book.class))).thenReturn(book);

        BookResponseDTO result = service.insert(bookRequestDTO);

        assertNotNull(result);
        assertEquals(book.getTitle(), result.getTitle());
        verify(repository, times(2)).save(any(Book.class));
        verify(s3Service, never()).uploadFiles(anyList(), anyString(), anyLong());
    }

    @Test
    void insertShouldUploadMediaWhenMediaIsProvided() {
        MultipartFile file = new MockMultipartFile("media", "cover.jpg", "image/jpeg", "data".getBytes());
        bookRequestDTO.setMedia(List.of(file));

        when(repository.findByIsbn(bookRequestDTO.getIsbn())).thenReturn(Optional.empty());
        when(categoryRepository.findById(bookRequestDTO.getCategoryId())).thenReturn(Optional.of(category));
        when(repository.save(any(Book.class))).thenReturn(book);
        when(s3Service.uploadFiles(anyList(), eq("books"), any()))
                .thenReturn(List.of("https://bucket.s3.amazonaws.com/books/1_1.jpg"));

        BookResponseDTO result = service.insert(bookRequestDTO);

        assertNotNull(result);
        verify(s3Service).uploadFiles(anyList(), eq("books"), any());
    }

    @Test
    void insertShouldThrowBusinessExceptionWhenIsbnAlreadyExists() {
        Book existing = BookFactory.createBook(5L, "Other", 2, 2);
        existing.setIsbn(bookRequestDTO.getIsbn());
        when(repository.findByIsbn(bookRequestDTO.getIsbn())).thenReturn(Optional.of(existing));

        assertThrows(BusinessException.class, () -> service.insert(bookRequestDTO));
        verify(repository, never()).save(any(Book.class));
    }

    @Test
    void insertShouldThrowBusinessExceptionWhenPublicationYearIsNull() {
        bookRequestDTO.setPublicationYear(null);
        when(repository.findByIsbn(bookRequestDTO.getIsbn())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> service.insert(bookRequestDTO));
    }

    @Test
    void insertShouldThrowBusinessExceptionWhenPublicationYearIsInvalid() {
        bookRequestDTO.setPublicationYear(3000);
        when(repository.findByIsbn(bookRequestDTO.getIsbn())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> service.insert(bookRequestDTO));
    }

    @Test
    void insertShouldThrowBusinessExceptionWhenCategoryIdIsNull() {
        bookRequestDTO.setCategoryId(null);
        when(repository.findByIsbn(bookRequestDTO.getIsbn())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> service.insert(bookRequestDTO));
    }

    @Test
    void insertShouldThrowResourceNotFoundExceptionWhenCategoryNotFound() {
        when(repository.findByIsbn(bookRequestDTO.getIsbn())).thenReturn(Optional.empty());
        when(categoryRepository.findById(bookRequestDTO.getCategoryId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.insert(bookRequestDTO));
    }

    @Test
    void insertShouldSkipDuplicateCheckWhenIsbnIsBlank() {
        bookRequestDTO.setIsbn("");
        when(categoryRepository.findById(bookRequestDTO.getCategoryId())).thenReturn(Optional.of(category));
        when(repository.save(any(Book.class))).thenReturn(book);

        BookResponseDTO result = service.insert(bookRequestDTO);

        assertNotNull(result);
        verify(repository, never()).findByIsbn(anyString());
    }

    @Test
    void updateShouldReturnBookResponseDTOWhenIdExists() {
        when(repository.findByIsbn(bookRequestDTO.getIsbn())).thenReturn(Optional.empty());
        when(repository.getReferenceById(existingId)).thenReturn(book);
        when(categoryRepository.findById(bookRequestDTO.getCategoryId())).thenReturn(Optional.of(category));
        when(repository.save(any(Book.class))).thenReturn(book);

        BookResponseDTO result = service.update(existingId, bookRequestDTO);

        assertNotNull(result);
        verify(repository).save(any(Book.class));
    }

    @Test
    void updateShouldReplaceMediaWhenNewMediaProvided() {
        Book bookWithMedia = BookFactory.createBook();
        bookWithMedia.getMedia().add(new Media("https://bucket.s3.amazonaws.com/books/old.jpg", 1));

        MultipartFile file = new MockMultipartFile("media", "new.jpg", "image/jpeg", "data".getBytes());
        bookRequestDTO.setMedia(List.of(file));

        when(repository.findByIsbn(bookRequestDTO.getIsbn())).thenReturn(Optional.empty());
        when(repository.getReferenceById(existingId)).thenReturn(bookWithMedia);
        when(categoryRepository.findById(bookRequestDTO.getCategoryId())).thenReturn(Optional.of(category));
        when(repository.save(any(Book.class))).thenReturn(bookWithMedia);
        when(s3Service.uploadFiles(anyList(), eq("books"), any()))
                .thenReturn(List.of("https://bucket.s3.amazonaws.com/books/1_1.jpg"));

        BookResponseDTO result = service.update(existingId, bookRequestDTO);

        assertNotNull(result);
        verify(s3Service).deleteFile(anyString());
        verify(s3Service).uploadFiles(anyList(), eq("books"), any());
    }

    @Test
    void updateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(repository.findByIsbn(bookRequestDTO.getIsbn())).thenReturn(Optional.empty());
        when(repository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);

        assertThrows(ResourceNotFoundException.class,
                () -> service.update(nonExistingId, bookRequestDTO));
    }

    @Test
    void updateShouldThrowBusinessExceptionWhenIsbnBelongsToAnotherBook() {
        Book another = BookFactory.createBook(7L, "Another", 1, 1);
        another.setIsbn(bookRequestDTO.getIsbn());
        when(repository.findByIsbn(bookRequestDTO.getIsbn())).thenReturn(Optional.of(another));

        assertThrows(BusinessException.class, () -> service.update(existingId, bookRequestDTO));
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
    void getBookShouldReturnBookEntityWhenIdExists() {
        when(repository.findById(existingId)).thenReturn(Optional.of(book));

        Book result = service.getBook(existingId);

        assertNotNull(result);
        assertEquals(existingId, result.getId());
    }

    @Test
    void getBookShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getBook(nonExistingId));
    }
}
