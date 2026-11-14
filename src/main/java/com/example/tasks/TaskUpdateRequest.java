package com.example.tasks;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * JSON body for PUT /tasks/{id}. Has no id field: the id always comes from the path.
 *
 * @param title new title, must not be blank
 * @param done  new completion status, required (PUT is a full replacement)
 */
public record TaskUpdateRequest(@NotBlank String title, @NotNull Boolean done) {
}
