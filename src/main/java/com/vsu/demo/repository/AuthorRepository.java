package com.vsu.demo.repository;

import com.vsu.demo.entity.Author;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AuthorRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public AuthorRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    public Optional<Author> findAuthorByName(String name) {
        String sql = "SELECT id, name FROM authors WHERE name = :name";

        List<Author> authors = namedParameterJdbcTemplate.query(
                sql,
                Map.of("name", name),
                AUTHOR_ROW_MAPPER
        );

        if (authors.size() > 1) {
            throw new RuntimeException("Found more than one author with name: " + name);
        }

        return authors.stream().findFirst();
    }

    public void lockOnValue(Object value) {
        String sql = "SELECT pg_advisory_xact_lock(hashtext(:lock));";

        namedParameterJdbcTemplate.queryForObject(
                sql,
                Map.of("lock", value.toString()),
                Object.class
        );
    }

    public boolean createAuthor(Author author) {
        String sql = """
                INSERT INTO authors (id, name)
                VALUES (:id, :name)
                """;

        return namedParameterJdbcTemplate.update(
                sql,
                Map.of(
                        "id", author.id(),
                        "name", author.name()
                )
        ) == 1;
    }
    public Optional<Author> findAuthorById(UUID id) {
        String sql = """
                SELECT id, name
                FROM authors
                WHERE id = :id
                """;

        List<Author> authors = namedParameterJdbcTemplate.query(
                sql,
                Map.of("id", id),
                AUTHOR_ROW_MAPPER
        );

        return authors.stream().findFirst();
    }

    public List<Author> findAllAuthors() {
        String sql = """
                SELECT id, name
                FROM authors
                ORDER BY name
                """;

        return namedParameterJdbcTemplate.query(
                sql,
                Map.of(),
                AUTHOR_ROW_MAPPER
        );
    }

    public boolean updateAuthor(Author author) {
        String sql = """
                UPDATE authors
                SET name = :name
                WHERE id = :id
                """;

        return namedParameterJdbcTemplate.update(
                sql,
                Map.of(
                        "id", author.id(),
                        "name", author.name()
                )
        ) == 1;
    }

    public boolean deleteAuthor(UUID id) {
        String sql = """
                DELETE FROM authors
                WHERE id = :id
                """;

        return namedParameterJdbcTemplate.update(
                sql,
                Map.of("id", id)
        ) == 1;
    }


    private static final RowMapper<Author> AUTHOR_ROW_MAPPER = (rs, rowNum) -> new Author(
            rs.getObject("id", UUID.class),
            rs.getString("name")
    );
}
