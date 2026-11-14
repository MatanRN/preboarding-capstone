package com.example.tasks;

import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;



@RestController
@RequestMapping("/tasks")
public class TasksController {

    private final TasksMemory tasksMemory;

    public TasksController(TasksMemory tasksMemory){
        this.tasksMemory = tasksMemory;
    }

    @GetMapping()
    public ArrayList<Task> getAllTasks(){
        return tasksMemory.getAll();
    }
    
    @GetMapping("/{id}")
    @ResponseStatus(value=HttpStatus.OK)
    public Task getTaskById(@PathVariable long id){
        Task res = tasksMemory.get(id);
        if(res == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "A task corresponding to the provided id doesn't exist");
        }
        else{
            return res;
        }

    }

    @PostMapping()
    @ResponseStatus(value = HttpStatus.CREATED)
    public void createTask(long id,String title,boolean done){
        try{
            Task t = new Task(id, title, done);
            tasksMemory.add(t);
        }
        catch(IllegalArgumentException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task title parameter cannot be set to null or blank");
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(value=HttpStatus.NO_CONTENT)
    public void deleteTaskById(@PathVariable long id){
        tasksMemory.remove(id);
    }
}

