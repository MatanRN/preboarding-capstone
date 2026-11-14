package com.example.tasks;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.example.tasks.records.Task;
import com.example.tasks.records.TaskCount;

@Component 
public class TasksMemory {
    
    private final Map<Long,Task> tasksMap;
    // Guarded by "this": only read or written inside synchronized methods.
    private long done;

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

    public synchronized Task add(Task t){
        Task prev = tasksMap.put(t.id(), t);
        adjustDone(prev, t);
        return prev;
    }

    /**
     * Replaces the task with the same id, only if one exists.
     * Atomic: never creates a task.
     *
     * @return the previous task, or null if no task had that id
     */
    public synchronized Task update(Task t){
        Task prev = tasksMap.replace(t.id(), t);
        if (prev != null) {
            adjustDone(prev, t);
        }
        return prev;
    }

    public synchronized Task remove(long id){
        Task prev = tasksMap.remove(id);
        adjustDone(prev, null);
        return prev;
    }


    
    public synchronized TaskCount count(){
        return new TaskCount(tasksMap.size(), done);
    }

    /**
     * Keeps the done counter in sync when a slot goes from prev to next.
     * Either may be null (no task before / no task after). Caller must hold the lock.
     */
    private void adjustDone(Task prev, Task next){
        if (prev != null && prev.done()) {
            done--;
        }
        if (next != null && next.done()) {
            done++;
        }
    }
}
