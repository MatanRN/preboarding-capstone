package com.example.tasks;

import java.util.List;

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
import com.example.tasks.records.TaskCreateRequest;
import com.example.tasks.records.TaskUpdateRequest;

@RestController
@RequestMapping("/tasks")
public class TasksController {

    private final TasksService tasksService;

    public TasksController(TasksService tasksService) {
        this.tasksService = tasksService;
    }

    @GetMapping()
    @ResponseStatus(value = HttpStatus.OK)
    public List<Task> getAllTasks(@RequestParam(required = false) Boolean done) {
        return tasksService.getAllTasks(done);
    }

    @GetMapping("/count")
    @ResponseStatus(value = HttpStatus.OK)
    public TaskCount getCount() {
        return tasksService.getCount();
    }

    @GetMapping("/{id}")
    @ResponseStatus(value = HttpStatus.OK)
    public Task getTaskById(@PathVariable long id) {
        return tasksService.getTaskById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "A task corresponding to the provided id doesn't exist"));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    public Task createTask(@Valid @RequestBody TaskCreateRequest request) {
        try {
            return tasksService.createTask(request);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Task title parameter cannot be set to null or blank");
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public Task updateTaskById(@PathVariable long id, @Valid @RequestBody TaskUpdateRequest request) {
        return tasksService.updateTask(id, request)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "A task corresponding to the provided id doesn't exist"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteTaskById(@PathVariable long id) {
        tasksService.deleteTask(id);
    }
}
