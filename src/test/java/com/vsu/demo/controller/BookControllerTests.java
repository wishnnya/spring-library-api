package com.vsu.demo.controller;

import com.vsu.demo.entity.Book;
import com.vsu.demo.request.BookRequest;
import com.vsu.demo.response.PageResponse;
import com.vsu.demo.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookControllerTests {

    private BookService bookService;
    private BookController bookController;

    @BeforeEach
    void setUp() {
        bookService = mock(BookService.class);
        bookController = new BookController(bookService);
    }

    @Test
    void shouldReturnBookWhenGettingById() {
        UUID bookId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        Book book = new Book(bookId, "Война и мир", authorId);

        when(bookService.findBookById(bookId)).thenReturn(book);

        Book result = bookController.getBookById(bookId);

        assertEquals(book, result);
        verify(bookService).findBookById(bookId);
    }

    @Test
    void shouldReturnAllBooks() {
        UUID authorId = UUID.randomUUID();

        List<Book> books = List.of(
                new Book(UUID.randomUUID(), "Война и мир", authorId),
                new Book(UUID.randomUUID(), "Анна Каренина", authorId)
        );

        when(bookService.findAllBooks()).thenReturn(books);

        List<Book> result = bookController.getAllBooks();

        assertEquals(books, result);
        verify(bookService).findAllBooks();
    }

    @Test
    void shouldReturnCreatedBookWhenCreating() {
        UUID authorId = UUID.randomUUID();

        BookRequest request = new BookRequest("Война и мир", authorId);
        Book book = new Book(UUID.randomUUID(), "Война и мир", authorId);

        when(bookService.createBook(request)).thenReturn(book);

        Book result = bookController.createBook(request);

        assertEquals(book, result);
        verify(bookService).createBook(request);
    }

    @Test
    void shouldReturnUpdatedBookWhenUpdating() {
        UUID bookId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        BookRequest request = new BookRequest("Новое название", authorId);
        Book book = new Book(bookId, "Новое название", authorId);

        when(bookService.updateBook(bookId, request)).thenReturn(book);

        Book result = bookController.updateBook(bookId, request);

        assertEquals(book, result);
        verify(bookService).updateBook(bookId, request);
    }

    @Test
    void shouldCallServiceWhenDeleting() {
        UUID bookId = UUID.randomUUID();

        bookController.deleteBook(bookId);

        verify(bookService).deleteBook(bookId);
    }

    @Test
    void shouldReturnBooksByTitleWhenSearching() {
        UUID authorId = UUID.randomUUID();

        List<Book> books = List.of(
                new Book(UUID.randomUUID(), "Война и мир", authorId)
        );

        when(bookService.searchBooksByTitle("Война")).thenReturn(books);

        List<Book> result = bookController.searchBooks("Война");

        assertEquals(books, result);
        verify(bookService).searchBooksByTitle("Война");
    }

    @Test
    void shouldReturnPageResponseWhenSearchingByAuthor() {
        UUID authorId = UUID.randomUUID();

        List<Book> books = List.of(
                new Book(UUID.randomUUID(), "Война и мир", authorId),
                new Book(UUID.randomUUID(), "Анна Каренина", authorId)
        );

        PageResponse<Book> response = new PageResponse<>(books, 2, 0);

        when(bookService.searchBooksByAuthor("Толстой", 0, 2))
                .thenReturn(response);

        PageResponse<Book> result =
                bookController.searchBooksAuthors("Толстой", 0, 2);

        assertEquals(response, result);
        verify(bookService).searchBooksByAuthor("Толстой", 0, 2);
    }
}