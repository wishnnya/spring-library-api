package com.vsu.demo.service;

import com.vsu.demo.entity.Author;
import com.vsu.demo.exception.ValidationException;
import com.vsu.demo.repository.AuthorRepository;
import com.vsu.demo.request.AuthorRequest;
import com.vsu.demo.response.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthorServiceTests {

    private AuthorRepository authorRepository;

    private AuthorService authorService;

    @BeforeEach
    void setUp() {
        authorRepository = mock(AuthorRepository.class);
        authorService = new AuthorService(authorRepository);
    }

    @Test
    void shouldCreateAuthorWhenAuthorDoesNotExist() {
        AuthorRequest request = new AuthorRequest("Толстой");

        when(authorRepository.findAuthorByName("Толстой")).thenReturn(Optional.empty());
        when(authorRepository.createAuthor(any(Author.class))).thenReturn(true);

        Author result = authorService.createAuthor(request);

        assertNotNull(result.id());
        assertEquals("Толстой", result.name());

        verify(authorRepository).lockOnValue("Толстой");
        verify(authorRepository).findAuthorByName("Толстой");
        verify(authorRepository).createAuthor(any(Author.class));
    }

    @Test
    void shouldThrowExceptionWhenAuthorAlreadyExistsCreate(){
        AuthorRequest request = new AuthorRequest("Толстой");
        Author existingAuthor = new Author(UUID.randomUUID(), "Толстой");

        when(authorRepository.findAuthorByName("Толстой")).thenReturn(Optional.of(existingAuthor));
        ValidationException validationException = assertThrows(
                ValidationException.class, () -> authorService.createAuthor(request)
        );

        assertEquals(ErrorCode.AUTHOR_ALREADY_EXISTS, validationException.getErrorCode());
        verify(authorRepository).lockOnValue("Толстой");
        verify(authorRepository).findAuthorByName("Толстой");
        verify(authorRepository, never()).createAuthor(any(Author.class));
    }

    @Test
    void shouldThrowRuntimeExceptionWhenRepositoryCannotCreateAuthor(){
        AuthorRequest request = new AuthorRequest("Толстой");

        when(authorRepository.findAuthorByName("Толстой")).thenReturn(Optional.empty());
        when(authorRepository.createAuthor(any(Author.class))).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authorService.createAuthor(request));
        verify(authorRepository).lockOnValue("Толстой");
        verify(authorRepository).findAuthorByName("Толстой");
        verify(authorRepository).createAuthor(any(Author.class));
    }

    @Test
    void shouldReturnAuthorWhenAuthorExistsFind(){
        UUID id = UUID.randomUUID();
        Author author = new Author(id, "Толстой");

        when(authorRepository.findAuthorById(id)).thenReturn(Optional.of(author));

        Author result = authorService.findAuthorById(id);

        assertEquals(id, result.id());
        assertEquals("Толстой", result.name());
        verify(authorRepository).findAuthorById(id);
    }

    @Test
    void shouldThrowExceptionWhenAuthorDoesNotExistFind(){
        UUID id = UUID.randomUUID();
        when(authorRepository.findAuthorById(id)).thenReturn(Optional.empty());

        ValidationException validationException = assertThrows(
                ValidationException.class, () -> authorService.findAuthorById(id)
        );

        assertEquals(ErrorCode.AUTHOR_NOT_FOUND, validationException.getErrorCode());
        verify(authorRepository).findAuthorById(id);
    }

    @Test
    void shouldReturnAllAuthors(){
        List<Author> authors = List.of(
                new Author(UUID.randomUUID(), "Толстой"),
                new Author(UUID.randomUUID(), "Достоевский")
        );

        when(authorRepository.findAllAuthors()).thenReturn(authors);
        List<Author> result = authorService.findAllAuthors();

        assertEquals(2, result.size());
        assertEquals(authors, result);
        verify(authorRepository).findAllAuthors();
    }

    @Test
    void shouldUpdateAuthorWhenAuthorExist(){
        UUID id = UUID.randomUUID();
        AuthorRequest authorRequest = new AuthorRequest("Новый автор");

        when(authorRepository.updateAuthor(any(Author.class))).thenReturn(true);
        Author result = authorService.updateAuthor(id, authorRequest);

        assertEquals(id, result.id());
        assertEquals("Новый автор", result.name());

        ArgumentCaptor<Author> captor = ArgumentCaptor.forClass(Author.class);
        verify(authorRepository).updateAuthor(captor.capture());

        Author savedAuthor = captor.getValue();

        assertEquals(id, savedAuthor.id());
        assertEquals("Новый автор", savedAuthor.name());
    }

    @Test
    void shouldThrowExceptionWhenAuthorDoesNotExistUpdate(){
        UUID id = UUID.randomUUID();
        AuthorRequest request = new AuthorRequest("Новый автор");

        when(authorRepository.updateAuthor(any(Author.class))).thenReturn(false);

        ValidationException validationException = assertThrows(
                ValidationException.class,() -> authorService.updateAuthor(id, request)
        );

        assertEquals(ErrorCode.AUTHOR_NOT_FOUND, validationException.getErrorCode());
        verify(authorRepository).updateAuthor(any(Author.class));
    }

    @Test
    void shouldDeleteAuthorWhenAuthorExists(){
        UUID id = UUID.randomUUID();

        when(authorRepository.deleteAuthor(id)).thenReturn(true);

        assertDoesNotThrow(() -> authorService.deleteAuthor(id));
        verify(authorRepository).deleteAuthor(id);
    }

    @Test
    void deleteAuthor_shouldThrowException_whenAuthorDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(authorRepository.deleteAuthor(id)).thenReturn(false);

        ValidationException exception = assertThrows(
                ValidationException.class,() -> authorService.deleteAuthor(id)
        );

        assertEquals(ErrorCode.AUTHOR_NOT_FOUND, exception.getErrorCode());
        verify(authorRepository).deleteAuthor(id);
    }
}
