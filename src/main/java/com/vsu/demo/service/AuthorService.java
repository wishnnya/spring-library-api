package com.vsu.demo.service;

import com.vsu.demo.entity.Author;
import com.vsu.demo.exception.ValidationException;
import com.vsu.demo.repository.AuthorRepository;
import com.vsu.demo.request.AuthorRequest;
import com.vsu.demo.response.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @Transactional
    public Author createAuthor(AuthorRequest request) {

        authorRepository.lockOnValue(request.name());

        Optional<Author> maybeAuthor = authorRepository.findAuthorByName(request.name());

        if (maybeAuthor.isPresent()) {
            throw new ValidationException(ErrorCode.AUTHOR_ALREADY_EXISTS);
        }

        Author newAuthor = new Author(
                UUID.randomUUID(),
                request.name()
        );

        if (!authorRepository.createAuthor(newAuthor)) {
            throw new RuntimeException();
        }

        return newAuthor;
    }

    public Author findAuthorById(UUID id) {
        return authorRepository.findAuthorById(id)
                .orElseThrow(() -> new ValidationException(ErrorCode.AUTHOR_NOT_FOUND));
    }

    public List<Author> findAllAuthors() {
        return authorRepository.findAllAuthors();
    }

    public Author updateAuthor(UUID id, AuthorRequest request) {
        Author author = new Author(
                id,
                request.name()
        );

        if (!authorRepository.updateAuthor(author)) {
            throw new ValidationException(ErrorCode.AUTHOR_NOT_FOUND);
        }

        return author;
    }

    public void deleteAuthor(UUID id) {
        if (!authorRepository.deleteAuthor(id)) {
            throw new ValidationException(ErrorCode.AUTHOR_NOT_FOUND);
        }
    }
}