package com.vsu.demo.service;

import com.vsu.demo.entity.Author;
import com.vsu.demo.entity.Book;
import com.vsu.demo.exception.ValidationException;
import com.vsu.demo.repository.AuthorRepository;
import com.vsu.demo.repository.BookRepository;
import com.vsu.demo.request.BookRequest;
import com.vsu.demo.response.ErrorCode;
import com.vsu.demo.response.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BookServiceTests {

    private BookRepository bookRepository;
    private AuthorRepository authorRepository;

    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookRepository = mock(BookRepository.class);
        authorRepository = mock(AuthorRepository.class);
        bookService = new BookService(bookRepository, authorRepository);
    }

    @Test
    void shouldCreateBookWhenAuthorExistsAndBookDoesNotExist(){
        UUID authorId = UUID.randomUUID();
        BookRequest request = new BookRequest("Война и мир", authorId);

        when(authorRepository.findAuthorById(authorId))
                .thenReturn(Optional.of(new Author(authorId, "Толстой")));

        when(bookRepository.findBookByTitle("Война и мир"))
                .thenReturn(Optional.empty());

        when(bookRepository.createBook(any(Book.class)))
                .thenReturn(true);

        Book result = bookService.createBook(request);

        assertNotNull(result.id());
        assertEquals("Война и мир", result.title());
        assertEquals(authorId, result.authorId());

        verify(authorRepository).findAuthorById(authorId);
        verify(bookRepository).lockOnValue("Война и мир");
        verify(bookRepository).findBookByTitle("Война и мир");
        verify(bookRepository).createBook(any(Book.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingBookWithNonExistentAuthor(){
        UUID authorId = UUID.randomUUID();
        BookRequest request = new BookRequest("Война и мир", authorId);

        when(authorRepository.findAuthorById(authorId))
                .thenReturn(Optional.empty());

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookService.createBook(request)
        );

        assertEquals(ErrorCode.AUTHOR_NOT_FOUND, exception.getErrorCode());

        verify(authorRepository).findAuthorById(authorId);
        verify(bookRepository, never()).lockOnValue(any());
        verify(bookRepository, never()).findBookByTitle(anyString());
        verify(bookRepository, never()).createBook(any(Book.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingBookThatAlreadyExists() {
        UUID authorId = UUID.randomUUID();
        BookRequest request = new BookRequest("Война и мир", authorId);
        Book existingBook = new Book(UUID.randomUUID(), "Война и мир", authorId);

        when(authorRepository.findAuthorById(authorId))
                .thenReturn(Optional.of(new Author(authorId, "Толстой")));

        when(bookRepository.findBookByTitle("Война и мир"))
                .thenReturn(Optional.of(existingBook));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookService.createBook(request)
        );

        assertEquals(ErrorCode.BOOK_ALREADY_EXISTS, exception.getErrorCode());

        verify(authorRepository).findAuthorById(authorId);
        verify(bookRepository).lockOnValue("Война и мир");
        verify(bookRepository).findBookByTitle("Война и мир");
        verify(bookRepository, never()).createBook(any(Book.class));
    }

    @Test
    void shouldThrowRuntimeExceptionWhenRepositoryCannotCreateBook() {
        UUID authorId = UUID.randomUUID();
        BookRequest request = new BookRequest("Война и мир", authorId);

        when(authorRepository.findAuthorById(authorId))
                .thenReturn(Optional.of(new Author(authorId, "Толстой")));

        when(bookRepository.findBookByTitle("Война и мир"))
                .thenReturn(Optional.empty());

        when(bookRepository.createBook(any(Book.class)))
                .thenReturn(false);

        assertThrows(
                RuntimeException.class,
                () -> bookService.createBook(request)
        );

        verify(authorRepository).findAuthorById(authorId);
        verify(bookRepository).lockOnValue("Война и мир");
        verify(bookRepository).findBookByTitle("Война и мир");
        verify(bookRepository).createBook(any(Book.class));
    }

    @Test
    void shouldReturnBookWhenBookExistsFind() {
        UUID bookId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        Book book = new Book(bookId, "Война и мир", authorId);

        when(bookRepository.findBookById(bookId))
                .thenReturn(Optional.of(book));

        Book result = bookService.findBookById(bookId);

        assertEquals(bookId, result.id());
        assertEquals("Война и мир", result.title());
        assertEquals(authorId, result.authorId());

        verify(bookRepository).findBookById(bookId);
    }

    @Test
    void 	shouldThrowExceptionWhenBookDoesNotExistFind() {
        UUID bookId = UUID.randomUUID();

        when(bookRepository.findBookById(bookId))
                .thenReturn(Optional.empty());

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookService.findBookById(bookId)
        );

        assertEquals(ErrorCode.BOOK_NOT_FOUND, exception.getErrorCode());

        verify(bookRepository).findBookById(bookId);
    }

    @Test
    void shouldReturnAllBooks() {
        UUID authorId = UUID.randomUUID();

        List<Book> books = List.of(
                new Book(UUID.randomUUID(), "Война и мир", authorId),
                new Book(UUID.randomUUID(), "Анна Каренина", authorId)
        );

        when(bookRepository.findAllBooks())
                .thenReturn(books);

        List<Book> result = bookService.findAllBooks();

        assertEquals(2, result.size());
        assertEquals(books, result);

        verify(bookRepository).findAllBooks();
    }

    @Test
    void shouldUpdateBookWhenBookAndAuthorExist() {
        UUID bookId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        BookRequest request = new BookRequest("Новое название", authorId);

        Book oldBook = new Book(bookId, "Старое название", authorId);
        Author author = new Author(authorId, "Толстой");

        when(bookRepository.findBookById(bookId))
                .thenReturn(Optional.of(oldBook));

        when(authorRepository.findAuthorById(authorId))
                .thenReturn(Optional.of(author));

        when(bookRepository.updateBook(any(Book.class)))
                .thenReturn(true);

        Book result = bookService.updateBook(bookId, request);

        assertEquals(bookId, result.id());
        assertEquals("Новое название", result.title());
        assertEquals(authorId, result.authorId());

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).updateBook(captor.capture());

        Book updatedBook = captor.getValue();

        assertEquals(bookId, updatedBook.id());
        assertEquals("Новое название", updatedBook.title());
        assertEquals(authorId, updatedBook.authorId());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentBook() {
        UUID bookId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        BookRequest request = new BookRequest("Новое название", authorId);

        when(bookRepository.findBookById(bookId))
                .thenReturn(Optional.empty());

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookService.updateBook(bookId, request)
        );

        assertEquals(ErrorCode.BOOK_NOT_FOUND, exception.getErrorCode());

        verify(bookRepository).findBookById(bookId);
        verify(authorRepository, never()).findAuthorById(any());
        verify(bookRepository, never()).updateBook(any(Book.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingBookWithNonExistentAuthor() {
        UUID bookId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        BookRequest request = new BookRequest("Новое название", authorId);

        Book oldBook = new Book(bookId, "Старое название", authorId);

        when(bookRepository.findBookById(bookId))
                .thenReturn(Optional.of(oldBook));

        when(authorRepository.findAuthorById(authorId))
                .thenReturn(Optional.empty());

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookService.updateBook(bookId, request)
        );

        assertEquals(ErrorCode.AUTHOR_NOT_FOUND, exception.getErrorCode());

        verify(bookRepository).findBookById(bookId);
        verify(authorRepository).findAuthorById(authorId);
        verify(bookRepository, never()).updateBook(any(Book.class));
    }

    @Test
    void shouldThrowExceptionWhenRepositoryCannotUpdateBook() {
        UUID bookId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        BookRequest request = new BookRequest("Новое название", authorId);

        Book oldBook = new Book(bookId, "Старое название", authorId);
        Author author = new Author(authorId, "Толстой");

        when(bookRepository.findBookById(bookId))
                .thenReturn(Optional.of(oldBook));

        when(authorRepository.findAuthorById(authorId))
                .thenReturn(Optional.of(author));

        when(bookRepository.updateBook(any(Book.class)))
                .thenReturn(false);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookService.updateBook(bookId, request)
        );

        assertEquals(ErrorCode.BOOK_NOT_FOUND, exception.getErrorCode());

        verify(bookRepository).findBookById(bookId);
        verify(authorRepository).findAuthorById(authorId);
        verify(bookRepository).updateBook(any(Book.class));
    }

    @Test
    void shouldDeleteBookWhenBookExists() {
        UUID bookId = UUID.randomUUID();

        when(bookRepository.deleteBook(bookId))
                .thenReturn(true);

        assertDoesNotThrow(() -> bookService.deleteBook(bookId));

        verify(bookRepository).deleteBook(bookId);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentBook() {
        UUID bookId = UUID.randomUUID();

        when(bookRepository.deleteBook(bookId))
                .thenReturn(false);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookService.deleteBook(bookId)
        );

        assertEquals(ErrorCode.BOOK_NOT_FOUND, exception.getErrorCode());

        verify(bookRepository).deleteBook(bookId);
    }

    @Test
    void shouldSearchBooksByTitle() {
        UUID authorId = UUID.randomUUID();

        List<Book> books = List.of(
                new Book(UUID.randomUUID(), "Война и мир", authorId)
        );

        when(bookRepository.searchBooksByTitle("Война"))
                .thenReturn(books);

        List<Book> result = bookService.searchBooksByTitle("Война");

        assertEquals(1, result.size());
        assertEquals(books, result);

        verify(bookRepository).searchBooksByTitle("Война");
    }

    @Test
    void shouldReturnPageResponseWhenSearchingBooksByAuthor() {
        UUID authorId = UUID.randomUUID();

        List<Book> books = List.of(
                new Book(UUID.randomUUID(), "Война и мир", authorId),
                new Book(UUID.randomUUID(), "Анна Каренина", authorId)
        );

        when(bookRepository.searchBooksByAuthor("Толстой", 1, 10))
                .thenReturn(books);

        when(bookRepository.countBooksByAuthor("Толстой"))
                .thenReturn(2);

        PageResponse<Book> result = bookService.searchBooksByAuthor("Толстой", 1, 10);

        assertEquals(books, result.data);
        assertEquals(2, result.total);
        assertEquals(1, result.currentPage);

        verify(bookRepository).searchBooksByAuthor("Толстой", 1, 10);
        verify(bookRepository).countBooksByAuthor("Толстой");
    }
}
