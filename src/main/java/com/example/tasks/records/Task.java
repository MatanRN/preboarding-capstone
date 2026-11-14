package com.example.tasks.records;

/**
 * Task entity record.
 * 
 * @param id
 * @param title
 * @param done
 */
public record Task(long id, String title, boolean done) {
    public Task {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Task title parameter cannot be set to null or blank");
        }
    }
}
