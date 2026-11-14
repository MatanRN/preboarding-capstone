package com.example.tasks;

import java.util.ArrayList;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.tasks.records.Task;
import com.example.tasks.records.TaskCount;
import com.example.tasks.records.TaskUpdateRequest;

@RestController
@RequestMapping("/tasks")
public class TasksController {

    private final TasksMemory tasksMemory;

    public TasksController(TasksMemory tasksMemory) {
        this.tasksMemory = tasksMemory;
    }

    @GetMapping()
    public ArrayList<Task> getAllTasks(@RequestParam(required = false) Boolean done) {
        if (done == null) {
            return tasksMemory.getAll();
        }
        return tasksMemory.getByDone(done);
    }

    @GetMapping("/count")
    @ResponseStatus(value = HttpStatus.OK)
    public TaskCount getCount() {
        return tasksMemory.count();
    }

    @GetMapping("/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public Task getTaskById(@PathVariable long id) {
        Task res = tasksMemory.get(id);
        if (res == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "A task corresponding to the provided id doesn't exist");
        } else {
            return res;
        }

    }

    @PostMapping()
    @ResponseStatus(value = HttpStatus.CREATED)
    public void createTask(long id, String title, boolean done) {
        try {
            Task t = new Task(id, title, done);
            tasksMemory.add(t);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Task title parameter cannot be set to null or blank");
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public Task updateTaskById(@PathVariable long id, @Valid @RequestBody TaskUpdateRequest request) {
        Task updated = new Task(id, request.title(), request.done());
        if (tasksMemory.update(updated) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "A task corresponding to the provided id doesn't exist");
        }
        return updated;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteTaskById(@PathVariable long id) {
        tasksMemory.remove(id);
    }
}
