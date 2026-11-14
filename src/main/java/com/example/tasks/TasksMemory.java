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

    public ArrayList<Task> getByDone(boolean done){
        ArrayList<Task> tasks = new ArrayList<>();
        for(Task t: tasksMap.values()){
            if(t.done() == done){
                tasks.add(t);
            }
        }
        return tasks;
    }

    public Task add(Task t){
        return tasksMap.put(t.id(), t);
    }

    /**
     * Replaces the task with the same id, only if one exists.
     * Atomic: never creates a task.
     *
     * @return the previous task, or null if no task had that id
     */
    public Task update(Task t){
        return tasksMap.replace(t.id(), t);
    }

    public Task remove(long id){
        return tasksMap.remove(id);
    }
}
