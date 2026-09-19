package com.vsu.demo.service;

import com.vsu.demo.entity.Book;
import com.vsu.demo.exception.ValidationException;
import com.vsu.demo.repository.AuthorRepository;
import com.vsu.demo.repository.BookRepository;
import com.vsu.demo.request.BookRequest;
import com.vsu.demo.response.ErrorCode;
import com.vsu.demo.response.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    public BookService(BookRepository bookRepository, AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
    }

    @Transactional
    public Book createBook(BookRequest request) {

        authorRepository.findAuthorById(request.authorId())
                .orElseThrow(() -> new ValidationException(ErrorCode.AUTHOR_NOT_FOUND));

        bookRepository.lockOnValue(request.title());

        Optional<Book> maybeBook = bookRepository.findBookByTitle(request.title());

        if (maybeBook.isPresent()) {
            throw new ValidationException(ErrorCode.BOOK_ALREADY_EXISTS);
        }

        Book newBook = new Book(
                UUID.randomUUID(),
                request.title(),
                request.authorId()
        );

        if (!bookRepository.createBook(newBook)) {
            throw new RuntimeException();
        }

        return newBook;
    }

    public Book findBookById(UUID id) {
        return bookRepository.findBookById(id)
                .orElseThrow(() -> new ValidationException(ErrorCode.BOOK_NOT_FOUND));
    }

    public List<Book> findAllBooks() {
        return bookRepository.findAllBooks();
    }

    public Book updateBook(UUID id, BookRequest request) {

        findBookById(id);

        authorRepository.findAuthorById(request.authorId())
                .orElseThrow(() -> new ValidationException(ErrorCode.AUTHOR_NOT_FOUND));

        Book book = new Book(
                id,
                request.title(),
                request.authorId()
        );

        if (!bookRepository.updateBook(book)) {
            throw new ValidationException(ErrorCode.BOOK_NOT_FOUND);
        }

        return book;
    }

    public void deleteBook(UUID id) {
        if (!bookRepository.deleteBook(id)) {
            throw new ValidationException(ErrorCode.BOOK_NOT_FOUND);
        }
    }

    public List<Book> searchBooksByTitle(String title) {
        return bookRepository.searchBooksByTitle(title);
    }

    public PageResponse<Book> searchBooksByAuthor(String name, int page, int size) {
        List<Book> books = bookRepository.searchBooksByAuthor(name, page, size);
        int total = bookRepository.countBooksByAuthor(name);
        return new PageResponse<>(books, total, page);
    }
}