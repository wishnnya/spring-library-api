package com.vsu.demo.request;

import java.util.UUID;

public record BookRequest(String title, UUID authorId) {
}
