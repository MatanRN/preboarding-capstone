package com.example.tasks;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.tasks.records.Task;
import com.example.tasks.records.TaskCount;
import com.example.tasks.records.TaskUpdateRequest;

@Service
public class TasksService {

    private final TasksMemory tasksMemory;

    public TasksService(TasksMemory tasksMemory) {
        this.tasksMemory = tasksMemory;
    }

    public List<Task> getAllTasks(Boolean done) {
        if (done == null) {
            return tasksMemory.getAll();
        }
        return tasksMemory.getByDone(done);
    }

    public TaskCount getCount() {
        return tasksMemory.count();
    }

    public Optional<Task> getTaskById(long id) {
        return Optional.ofNullable(tasksMemory.get(id));
    }

    public void createTask(long id, String title, boolean done) {
        Task task = new Task(id, title, done);
        tasksMemory.add(task);
    }

    public Optional<Task> updateTask(long id, TaskUpdateRequest request) {
        Task updated = new Task(id, request.title(), request.done());
        Task previous = tasksMemory.update(updated);
        return previous != null ? Optional.of(updated) : Optional.empty();
    }

    public void deleteTask(long id) {
        tasksMemory.remove(id);
    }
}
