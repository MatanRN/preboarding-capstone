package com.example.tasks.records;

import jakarta.validation.constraints.NotBlank;

public record TaskCreateRequest(
    @NotBlank String title,
    Boolean done
) {}
