package com.example.tasks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.tasks.records.Task;
import com.example.tasks.records.TaskCount;
import com.example.tasks.records.TaskUpdateRequest;

@WebMvcTest(TasksController.class)
class TasksControllerTests {
	String BASE_URL = "/tasks";
	Task dummyTask = new Task(1, "dummy", false);

	@Autowired
	private MockMvc mock;

	@MockitoBean
	private TasksService tasksService;

	@Nested
	class getAllTasks {
		ArrayList<Task> dummyTaskList = new ArrayList<>(
				List.of(dummyTask, new Task(2, "dummy2", false), new Task(3, "dummy3", true)));

		@Test
		void returnsAllExistingTasks() throws Exception {
			when(tasksService.getAllTasks(null)).thenReturn(dummyTaskList);
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
		void returnsNoTasks() throws Exception {
			when(tasksService.getAllTasks(null)).thenReturn(List.of());
			mock.perform(get(BASE_URL)).andExpect(status().isOk())
					.andExpect(jsonPath("$").isEmpty());
		}

		@Test
		void noDoneParamUsesGetAll() throws Exception {
			when(tasksService.getAllTasks(null)).thenReturn(dummyTaskList);
			mock.perform(get(BASE_URL)).andExpect(status().isOk())
					.andExpect(jsonPath("$.length()").value(3));
			verify(tasksService).getAllTasks(null);
		}

		@Test
		void doneTrueReturnsOnlyDoneTasks() throws Exception {
			when(tasksService.getAllTasks(true)).thenReturn(List.of(dummyTaskList.get(2)));
			mock.perform(get(BASE_URL).param("done", "true")).andExpect(status().isOk())
					.andExpect(jsonPath("$.length()").value(1))
					.andExpect(jsonPath("$[0].id").value(3))
					.andExpect(jsonPath("$[0].done").value(true));
			verify(tasksService).getAllTasks(true);
		}

		@Test
		void doneFalseReturnsOnlyUndoneTasks() throws Exception {
			when(tasksService.getAllTasks(false))
					.thenReturn(List.of(dummyTaskList.get(0), dummyTaskList.get(1)));
			mock.perform(get(BASE_URL).param("done", "false")).andExpect(status().isOk())
					.andExpect(jsonPath("$.length()").value(2))
					.andExpect(jsonPath("$[0].done").value(false))
					.andExpect(jsonPath("$[1].done").value(false));
			verify(tasksService).getAllTasks(false);
		}

		@Test
		void invalidDoneValueReturnsBadRequest() throws Exception {
			mock.perform(get(BASE_URL).param("done", "abc")).andExpect(status().isBadRequest());
			verify(tasksService, never()).getAllTasks(any());
		}

		@Test
		void doneValueIsCaseInsensitive() throws Exception {
			when(tasksService.getAllTasks(true)).thenReturn(List.of());
			mock.perform(get(BASE_URL).param("done", "TRUE")).andExpect(status().isOk());
			verify(tasksService).getAllTasks(true);
		}

		@Test
		void springAlternativeBooleanSpellingsAreAccepted() throws Exception {
			when(tasksService.getAllTasks(anyBoolean())).thenReturn(List.of());
			mock.perform(get(BASE_URL).param("done", "yes")).andExpect(status().isOk());
			verify(tasksService).getAllTasks(true);
			mock.perform(get(BASE_URL).param("done", "0")).andExpect(status().isOk());
			verify(tasksService).getAllTasks(false);
		}

		@Test
		void emptyDoneValueFallsBackToGetAll() throws Exception {
			when(tasksService.getAllTasks(null)).thenReturn(dummyTaskList);
			mock.perform(get(BASE_URL).param("done", "")).andExpect(status().isOk())
					.andExpect(jsonPath("$.length()").value(3));
			verify(tasksService).getAllTasks(null);
		}

		@Test
		void filterWithNoMatchesReturnsEmptyList() throws Exception {
			when(tasksService.getAllTasks(true)).thenReturn(List.of());
			mock.perform(get(BASE_URL).param("done", "true")).andExpect(status().isOk())
					.andExpect(jsonPath("$").isEmpty());
		}
	}

	@Nested
	class getCount {
		String url = BASE_URL + "/count";

		@Test
		void returnsTotalAndDoneCounts() throws Exception {
			when(tasksService.getCount()).thenReturn(new TaskCount(5, 2));
			mock.perform(get(url)).andExpect(status().isOk())
					.andExpect(jsonPath("$.total").value(5))
					.andExpect(jsonPath("$.done").value(2));
			verify(tasksService).getCount();
		}

		@Test
		void returnsZeroCountsWhenNoTasks() throws Exception {
			when(tasksService.getCount()).thenReturn(new TaskCount(0, 0));
			mock.perform(get(url)).andExpect(status().isOk())
					.andExpect(jsonPath("$.total").value(0))
					.andExpect(jsonPath("$.done").value(0));
			verify(tasksService).getCount();
		}
	}

	@Nested
	class getTaskByIdTests {
		String url = BASE_URL + "/" + dummyTask.id();

		@Test
		void nonExistingIdThrowsException() throws Exception {
			when(tasksService.getTaskById(1)).thenReturn(Optional.empty());
			mock.perform(get(url)).andExpect(status().isNotFound());
		}

		@Test
		void existingIdReturnsTask() throws Exception {
			when(tasksService.getTaskById(1)).thenReturn(Optional.of(dummyTask));
			mock.perform(get(url)).andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(dummyTask.id()))
					.andExpect(jsonPath("$.title").value(dummyTask.title()))
					.andExpect(jsonPath("$.done").value(dummyTask.done()));
		}
	}

	@Nested
	class createTask {
		@Test
		void taskCorrectlyAddedToMemory() throws Exception {
			mock.perform(post(BASE_URL).param("id", "1")
					.param("title", "dummy")
					.param("done", "false")).andExpect(status().isCreated());
			verify(tasksService).createTask(1L, "dummy", false);
		}

		@Test
		void nullTaskTitleThrowsException() throws Exception {
			doThrow(new IllegalArgumentException("Task title parameter cannot be set to null or blank"))
					.when(tasksService).createTask(eq(1L), isNull(), eq(false));
			mock.perform(post(BASE_URL).param("id", "1")
					.param("done", "false")).andExpect(status().isBadRequest());
		}

		@Test
		void blankTaskTitleThrowsException() throws Exception {
			doThrow(new IllegalArgumentException("Task title parameter cannot be set to null or blank"))
					.when(tasksService).createTask(eq(1L), eq(""), eq(false));
			mock.perform(post(BASE_URL).param("id", "1")
					.param("title", "")
					.param("done", "false")).andExpect(status().isBadRequest());
		}
	}

	@Nested
	class updateTaskById {
		String url = BASE_URL + "/" + dummyTask.id();

		@Test
		void existingTaskIsUpdated() throws Exception {
			when(tasksService.updateTask(eq(1L), any(TaskUpdateRequest.class)))
					.thenReturn(Optional.of(new Task(1, "new", true)));
			mock.perform(put(url).contentType(MediaType.APPLICATION_JSON)
					.content("{\"title\":\"new\",\"done\":true}"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(1))
					.andExpect(jsonPath("$.title").value("new"))
					.andExpect(jsonPath("$.done").value(true));
			verify(tasksService).updateTask(1L, new TaskUpdateRequest("new", true));
		}

		@Test
		void nonExistingTaskReturnsNotFound() throws Exception {
			when(tasksService.updateTask(eq(1L), any(TaskUpdateRequest.class))).thenReturn(Optional.empty());
			mock.perform(put(url).contentType(MediaType.APPLICATION_JSON)
					.content("{\"title\":\"new\",\"done\":true}"))
					.andExpect(status().isNotFound());
		}

		@Test
		void blankTitleReturnsBadRequest() throws Exception {
			mock.perform(put(url).contentType(MediaType.APPLICATION_JSON)
					.content("{\"title\":\"\",\"done\":true}"))
					.andExpect(status().isBadRequest());
			mock.perform(put(url).contentType(MediaType.APPLICATION_JSON)
					.content("{\"title\":\"   \",\"done\":true}"))
					.andExpect(status().isBadRequest());
			verify(tasksService, never()).updateTask(any(Long.class), any());
		}

		@Test
		void missingTitleReturnsBadRequest() throws Exception {
			mock.perform(put(url).contentType(MediaType.APPLICATION_JSON)
					.content("{\"done\":true}"))
					.andExpect(status().isBadRequest());
			verify(tasksService, never()).updateTask(any(Long.class), any());
		}

		@Test
		void missingDoneReturnsBadRequest() throws Exception {
			mock.perform(put(url).contentType(MediaType.APPLICATION_JSON)
					.content("{\"title\":\"new\"}"))
					.andExpect(status().isBadRequest());
			verify(tasksService, never()).updateTask(any(Long.class), any());
		}

		@Test
		void pathIdWinsOverBodyId() throws Exception {
			when(tasksService.updateTask(eq(1L), any(TaskUpdateRequest.class)))
					.thenReturn(Optional.of(new Task(1, "new", true)));
			mock.perform(put(url).contentType(MediaType.APPLICATION_JSON)
					.content("{\"id\":99,\"title\":\"new\",\"done\":true}"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(1));
			verify(tasksService).updateTask(1L, new TaskUpdateRequest("new", true));
		}

		@Test
		void nonNumericIdReturnsBadRequest() throws Exception {
			mock.perform(put(BASE_URL + "/abc").contentType(MediaType.APPLICATION_JSON)
					.content("{\"title\":\"new\",\"done\":true}"))
					.andExpect(status().isBadRequest());
			verify(tasksService, never()).updateTask(any(Long.class), any());
		}

		@Test
		void formEncodedBodyReturnsUnsupportedMediaType() throws Exception {
			mock.perform(put(url).contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("title", "new")
					.param("done", "true"))
					.andExpect(status().isUnsupportedMediaType());
			verify(tasksService, never()).updateTask(any(Long.class), any());
		}
	}

	@Nested
	class deleteTaskById {
		String url = BASE_URL + "/" + dummyTask.id();

		@Test
		void existingTaskCorrectlyRemovedFromMemory() throws Exception {
			mock.perform(delete(url)).andExpect(status().isNoContent());
			verify(tasksService).deleteTask(1);
		}

		@Test
		void nonExistingTaskDoesNothing() throws Exception {
			mock.perform(delete(url)).andExpect(status().isNoContent());
			verify(tasksService).deleteTask(1);
		}
	}
}
