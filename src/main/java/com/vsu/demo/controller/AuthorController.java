package com.vsu.demo.controller;

import com.vsu.demo.entity.Author;
import com.vsu.demo.request.AuthorRequest;
import com.vsu.demo.service.AuthorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/authors")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping("/{id}")
    public Author getAuthorById(@PathVariable UUID id) {
        return authorService.findAuthorById(id);
    }

    @GetMapping
    public List<Author> getAllAuthors() {
        return authorService.findAllAuthors();
    }

    @PostMapping
    public Author createAuthor(@RequestBody AuthorRequest request) {
        return authorService.createAuthor(request);
    }

    @PutMapping("/{id}")
    public Author updateAuthor(
            @PathVariable UUID id,
            @RequestBody AuthorRequest request) {

        return authorService.updateAuthor(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteAuthor(@PathVariable UUID id) {
        authorService.deleteAuthor(id);
    }
}