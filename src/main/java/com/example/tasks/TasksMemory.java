package com.example.tasks;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component 
public class TasksMemory {
    
    private final Map<Long,Task> tasksMap;

    public TasksMemory(){
        this.tasksMap = new ConcurrentHashMap<>();
    }

    public Task get(long id){
        return tasksMap.get(id);
    }

    public ArrayList<Task> getAll(){
        ArrayList<Task> tasks = new ArrayList<>();
        for(Task t: tasksMap.values()){
            Task taskCopy = new Task(t.id(), t.title(), t.done());
            tasks.add(taskCopy);
        }
        return tasks;
    }

    public Task add(Task t){
        return tasksMap.put(t.id(), t);
    }

    public Task remove(long id){
        return tasksMap.remove(id);
    }
}
