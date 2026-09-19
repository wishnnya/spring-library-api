package com.vsu.demo.entity;

import java.util.UUID;

public record Book(UUID id, String title, UUID authorId) {
}
