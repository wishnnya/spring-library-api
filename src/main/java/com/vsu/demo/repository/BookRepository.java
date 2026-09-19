package com.vsu.demo.repository;

import com.vsu.demo.entity.Book;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class BookRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public BookRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public Optional<Book> findBookByTitle(String title) {
        String sql = "SELECT id, title, author_id FROM books WHERE title = :title";

        List<Book> books = namedParameterJdbcTemplate.query(
                sql,
                Map.of("title", title),
                BOOK_ROW_MAPPER
        );

        if (books.size() > 1) {
            throw new RuntimeException("Found more than one book with title: " + title);
        }

        return books.stream().findFirst();
    }

    public void lockOnValue(Object value) {
        String sql = "SELECT pg_advisory_xact_lock(hashtext(:lock));";

        namedParameterJdbcTemplate.queryForObject(
                sql,
                Map.of("lock", value.toString()),
                Object.class
        );
    }

    public boolean createBook(Book book) {
        String sql = """
                INSERT INTO books (id, title, author_id)
                VALUES (:id, :title, :authorId)
                """;

        return namedParameterJdbcTemplate.update(
                sql,
                Map.of(
                        "id", book.id(),
                        "title", book.title(),
                        "authorId", book.authorId()
                )
        ) == 1;
    }

    public Optional<Book> findBookById(UUID id) {
        String sql = """
                SELECT id, title, author_id
                FROM books
                WHERE id = :id
                """;

        List<Book> books = namedParameterJdbcTemplate.query(
                sql,
                Map.of("id", id),
                BOOK_ROW_MAPPER
        );

        return books.stream().findFirst();
    }

    public List<Book> findAllBooks() {
        String sql = """
                SELECT id, title, author_id
                FROM books
                ORDER BY title
                """;

        return namedParameterJdbcTemplate.query(
                sql,
                Map.of(),
                BOOK_ROW_MAPPER
        );
    }

    public boolean updateBook(Book book) {
        String sql = """
                UPDATE books
                SET title = :title,
                    author_id = :authorId
                WHERE id = :id
                """;

        return namedParameterJdbcTemplate.update(
                sql,
                Map.of(
                        "id", book.id(),
                        "title", book.title(),
                        "authorId", book.authorId()
                )
        ) == 1;
    }

    public boolean deleteBook(UUID id) {
        String sql = """
                DELETE FROM books
                WHERE id = :id
                """;

        return namedParameterJdbcTemplate.update(
                sql,
                Map.of("id", id)
        ) == 1;
    }

    public List<Book> searchBooksByTitle(String title) {
        String sql = """
                SELECT id, title, author_id
                FROM books
                WHERE LOWER(title) LIKE LOWER(:title)
                ORDER BY title
                """;

        return namedParameterJdbcTemplate.query(
                sql,
                Map.of("title", title),
                BOOK_ROW_MAPPER
        );
    }

    public List<Book> searchBooksByAuthor(String name, int page, int size) {
        String sql = """
            SELECT books.id, books.title, books.author_id
            FROM books
            JOIN authors ON books.author_id = authors.id
            WHERE LOWER(authors.name) LIKE LOWER(:name)
            ORDER BY books.title
            LIMIT :size OFFSET :offset
            """;

        return namedParameterJdbcTemplate.query(
                sql,
                Map.of(
                        "name", "%" + name + "%",
                        "size", size,
                        "offset", page * size
                ),
                BOOK_ROW_MAPPER
        );
    }

    public int countBooksByAuthor(String name) {
        String sql = """
        SELECT COUNT(*)
        FROM books
        JOIN authors ON books.author_id = authors.id
        WHERE LOWER(authors.name) LIKE LOWER(:name)
        """;

        return namedParameterJdbcTemplate.queryForObject(
                sql,
                Map.of("name", "%" + name + "%"),
                Integer.class
        );
    }

    private static final RowMapper<Book> BOOK_ROW_MAPPER = (rs, rowNum) -> new Book(
            rs.getObject("id", UUID.class),
            rs.getString("title"),
            rs.getObject("author_id", UUID.class)
    );
}
