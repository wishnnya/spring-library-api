package com.vsu.demo.controller;

import com.vsu.demo.entity.Author;
import com.vsu.demo.request.AuthorRequest;
import com.vsu.demo.service.AuthorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthorControllerTests {

    private AuthorService authorService;
    private AuthorController authorController;

    @BeforeEach
    void setUp() {
        authorService = mock(AuthorService.class);
        authorController = new AuthorController(authorService);
    }

    @Test
    void shouldReturnAuthorWhenGettingById() {
        UUID id = UUID.randomUUID();
        Author author = new Author(id, "Толстой");

        when(authorService.findAuthorById(id)).thenReturn(author);

        Author result = authorController.getAuthorById(id);

        assertEquals(author, result);
        verify(authorService).findAuthorById(id);
    }

    @Test
    void shouldReturnAllAuthors() {
        List<Author> authors = List.of(
                new Author(UUID.randomUUID(), "Толстой"),
                new Author(UUID.randomUUID(), "Достоевский")
        );

        when(authorService.findAllAuthors()).thenReturn(authors);

        List<Author> result = authorController.getAllAuthors();

        assertEquals(authors, result);
        verify(authorService).findAllAuthors();
    }

    @Test
    void shouldReturnCreatedAuthorWhenCreating() {
        AuthorRequest request = new AuthorRequest("Толстой");
        Author author = new Author(UUID.randomUUID(), "Толстой");

        when(authorService.createAuthor(request)).thenReturn(author);

        Author result = authorController.createAuthor(request);

        assertEquals(author, result);
        verify(authorService).createAuthor(request);
    }

    @Test
    void shouldReturnUpdatedAuthorWhenUpdating() {
        UUID id = UUID.randomUUID();
        AuthorRequest request = new AuthorRequest("Новый автор");
        Author author = new Author(id, "Новый автор");

        when(authorService.updateAuthor(id, request)).thenReturn(author);

        Author result = authorController.updateAuthor(id, request);

        assertEquals(author, result);
        verify(authorService).updateAuthor(id, request);
    }

    @Test
    void shouldCallServiceWhenDeleting() {
        UUID id = UUID.randomUUID();

        authorController.deleteAuthor(id);

        verify(authorService).deleteAuthor(id);
    }
}