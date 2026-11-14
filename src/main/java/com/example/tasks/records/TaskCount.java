package com.example.tasks.records;

/**
 * Represents aggregate counts of tasks.
 *
 * @param total the total number of tasks
 * @param done  the number of completed tasks
 */
public record TaskCount(long total, long done) {
    public TaskCount {
        if (total < 0 || done < 0 || done > total) {
            throw new IllegalArgumentException("Invalid task count values");
        }
    }
}
