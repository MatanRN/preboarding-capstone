package com.example.tasks;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@WebMvcTest(TasksController.class)
class TasksControllerTests {
	String BASE_URL = "/tasks";
	Task dummyTask = new Task(1,"dummy",false);

	@Autowired
	private MockMvc mock;

	@MockitoBean
	private TasksMemory tasksMemory;

	@Nested
	class getAllTasks{
		ArrayList<Task> dummyTaskList = new ArrayList<>(
		List.of(dummyTask,new Task(2,"dummy2",false),new Task(3,"dummy3",true))
		);

		@Test
		void returnsAllExistingTasks() throws Exception{
			when(tasksMemory.getAll()).thenReturn(dummyTaskList);
			mock.perform(get(BASE_URL)).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(dummyTaskList.get(0).id()))
				.andExpect(jsonPath("$[0].title").value(dummyTaskList.get(0).title()))
				.andExpect(jsonPath("$[0].done").value(dummyTaskList.get(0).done()))
				.andExpect(jsonPath("$[1].id").value(dummyTaskList.get(1).id()))
				.andExpect(jsonPath("$[1].title").value(dummyTaskList.get(1).title()))
				.andExpect(jsonPath("$[1].done").value(dummyTaskList.get(1).done()))
				.andExpect(jsonPath("$[2].id").value(dummyTaskList.get(2).id()))
				.andExpect(jsonPath("$[2].title").value(dummyTaskList.get(2).title()))
				.andExpect(jsonPath("$[2].done").value(dummyTaskList.get(2).done()));
		}
		@Test
		void returnsNoTasks() throws Exception{
			when(tasksMemory.getAll()).thenReturn(new ArrayList<>());
			mock.perform(get(BASE_URL)).andExpect(status().isOk())
			.andExpect(jsonPath("$").isEmpty());
		}
	}

	@Nested
	class getTaskByIdTests{
		String url = BASE_URL+"/"+dummyTask.id();
		@Test
		void nonExistingIdThrowsException() throws Exception{
			when(tasksMemory.get(1)).thenReturn(null);
			mock.perform(get(url)).andExpect(status().isNotFound());
		}
		@Test
		void existingIdReturnsTask() throws Exception{
			when(tasksMemory.get(1)).thenReturn(dummyTask);
			mock.perform(get(url)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(dummyTask.id()))
				.andExpect(jsonPath("$.title").value(dummyTask.title()))
				.andExpect(jsonPath("$.done").value(dummyTask.done()));
		}
	}

	@Nested
	class createTask{
		@Test
		void taskCorrectlyAddedToMemory() throws Exception{
			mock.perform(post(BASE_URL).param("id","1")
				.param("title", "dummy")
				.param("done","false")
			).andExpect(status().isCreated());

		}
		@Test
		void nullTaskTitleThrowsException() throws Exception{
			mock.perform(post(BASE_URL).param("id","1")
				.param("done","false")
			).andExpect(status().isBadRequest());
		}
		@Test
		void blankTaskTitleThrowsException() throws Exception{
			mock.perform(post(BASE_URL).param("id","1")
				.param("title", "")
				.param("done","false")
			).andExpect(status().isBadRequest());
		}
	}

	@Nested
	class deleteTaskById{
		String url = BASE_URL+"/"+dummyTask.id();
		@Test
		void existingTaskCorrectlyRemovedFromMemory() throws Exception{
			when(tasksMemory.remove(1)).thenReturn(dummyTask);
			mock.perform(delete(url)
			).andExpect(status().isNoContent());
		}
		@Test
		void nonExistingTaskDoesNothing() throws Exception{
			when(tasksMemory.remove(1)).thenReturn(null);
			mock.perform(delete(url)
			).andExpect(status().isNoContent());
		}
	}
}
