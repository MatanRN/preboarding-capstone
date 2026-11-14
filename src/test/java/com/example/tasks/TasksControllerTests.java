package com.example.tasks;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.example.tasks.records.Task;
import com.example.tasks.records.TaskCount;

@WebMvcTest(TasksController.class)
class TasksControllerTests {
	String BASE_URL = "/tasks";
	Task dummyTask = new Task(1, "dummy", false);

	@Autowired
	private MockMvc mock;

	@MockitoBean
	private TasksMemory tasksMemory;

	@Nested
	class getAllTasks {
		ArrayList<Task> dummyTaskList = new ArrayList<>(
				List.of(dummyTask, new Task(2, "dummy2", false), new Task(3, "dummy3", true)));

		@Test
		void returnsAllExistingTasks() throws Exception {
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
		void returnsNoTasks() throws Exception {
			when(tasksMemory.getAll()).thenReturn(new ArrayList<>());
			mock.perform(get(BASE_URL)).andExpect(status().isOk())
					.andExpect(jsonPath("$").isEmpty());
		}

		@Test
		void noDoneParamUsesGetAll() throws Exception {
			when(tasksMemory.getAll()).thenReturn(dummyTaskList);
			mock.perform(get(BASE_URL)).andExpect(status().isOk())
					.andExpect(jsonPath("$.length()").value(3));
			verify(tasksMemory).getAll();
			verify(tasksMemory, never()).getByDone(anyBoolean());
		}

		@Test
		void doneTrueReturnsOnlyDoneTasks() throws Exception {
			when(tasksMemory.getByDone(true)).thenReturn(new ArrayList<>(List.of(dummyTaskList.get(2))));
			mock.perform(get(BASE_URL).param("done", "true")).andExpect(status().isOk())
					.andExpect(jsonPath("$.length()").value(1))
					.andExpect(jsonPath("$[0].id").value(3))
					.andExpect(jsonPath("$[0].done").value(true));
			verify(tasksMemory).getByDone(true);
			verify(tasksMemory, never()).getAll();
		}

		@Test
		void doneFalseReturnsOnlyUndoneTasks() throws Exception {
			when(tasksMemory.getByDone(false))
					.thenReturn(new ArrayList<>(List.of(dummyTaskList.get(0), dummyTaskList.get(1))));
			mock.perform(get(BASE_URL).param("done", "false")).andExpect(status().isOk())
					.andExpect(jsonPath("$.length()").value(2))
					.andExpect(jsonPath("$[0].done").value(false))
					.andExpect(jsonPath("$[1].done").value(false));
			verify(tasksMemory).getByDone(false);
			verify(tasksMemory, never()).getAll();
		}

		@Test
		void invalidDoneValueReturnsBadRequest() throws Exception {
			mock.perform(get(BASE_URL).param("done", "abc")).andExpect(status().isBadRequest());
			verify(tasksMemory, never()).getAll();
			verify(tasksMemory, never()).getByDone(anyBoolean());
		}

		@Test
		void doneValueIsCaseInsensitive() throws Exception {
			when(tasksMemory.getByDone(true)).thenReturn(new ArrayList<>());
			mock.perform(get(BASE_URL).param("done", "TRUE")).andExpect(status().isOk());
			verify(tasksMemory).getByDone(true);
		}

		@Test
		void springAlternativeBooleanSpellingsAreAccepted() throws Exception {
			when(tasksMemory.getByDone(anyBoolean())).thenReturn(new ArrayList<>());
			mock.perform(get(BASE_URL).param("done", "yes")).andExpect(status().isOk());
			verify(tasksMemory).getByDone(true);
			mock.perform(get(BASE_URL).param("done", "0")).andExpect(status().isOk());
			verify(tasksMemory).getByDone(false);
		}

		@Test
		void emptyDoneValueFallsBackToGetAll() throws Exception {
			when(tasksMemory.getAll()).thenReturn(dummyTaskList);
			mock.perform(get(BASE_URL).param("done", "")).andExpect(status().isOk())
					.andExpect(jsonPath("$.length()").value(3));
			verify(tasksMemory).getAll();
			verify(tasksMemory, never()).getByDone(anyBoolean());
		}

		@Test
		void filterWithNoMatchesReturnsEmptyList() throws Exception {
			when(tasksMemory.getByDone(true)).thenReturn(new ArrayList<>());
			mock.perform(get(BASE_URL).param("done", "true")).andExpect(status().isOk())
					.andExpect(jsonPath("$").isEmpty());
		}
	}

	@Nested
	class getTaskByIdTests {
		String url = BASE_URL + "/" + dummyTask.id();

		@Test
		void nonExistingIdThrowsException() throws Exception {
			when(tasksMemory.get(1)).thenReturn(null);
			mock.perform(get(url)).andExpect(status().isNotFound());
		}

		@Test
		void existingIdReturnsTask() throws Exception {
			when(tasksMemory.get(1)).thenReturn(dummyTask);
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

		}

		@Test
		void nullTaskTitleThrowsException() throws Exception {
			mock.perform(post(BASE_URL).param("id", "1")
					.param("done", "false")).andExpect(status().isBadRequest());
		}

		@Test
		void blankTaskTitleThrowsException() throws Exception {
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
			when(tasksMemory.update(any(Task.class))).thenReturn(dummyTask);
			mock.perform(put(url).contentType(MediaType.APPLICATION_JSON)
					.content("{\"title\":\"new\",\"done\":true}"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(1))
					.andExpect(jsonPath("$.title").value("new"))
					.andExpect(jsonPath("$.done").value(true));
			verify(tasksMemory).update(new Task(1, "new", true));
		}

		@Test
		void nonExistingTaskReturnsNotFound() throws Exception {
			when(tasksMemory.update(any(Task.class))).thenReturn(null);
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
			verify(tasksMemory, never()).update(any());
		}

		@Test
		void missingTitleReturnsBadRequest() throws Exception {
			mock.perform(put(url).contentType(MediaType.APPLICATION_JSON)
					.content("{\"done\":true}"))
					.andExpect(status().isBadRequest());
			verify(tasksMemory, never()).update(any());
		}

		@Test
		void missingDoneReturnsBadRequest() throws Exception {
			mock.perform(put(url).contentType(MediaType.APPLICATION_JSON)
					.content("{\"title\":\"new\"}"))
					.andExpect(status().isBadRequest());
			verify(tasksMemory, never()).update(any());
		}

		@Test
		void pathIdWinsOverBodyId() throws Exception {
			when(tasksMemory.update(any(Task.class))).thenReturn(dummyTask);
			mock.perform(put(url).contentType(MediaType.APPLICATION_JSON)
					.content("{\"id\":99,\"title\":\"new\",\"done\":true}"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(1));
			verify(tasksMemory).update(new Task(1, "new", true));
		}

		@Test
		void nonNumericIdReturnsBadRequest() throws Exception {
			mock.perform(put(BASE_URL + "/abc").contentType(MediaType.APPLICATION_JSON)
					.content("{\"title\":\"new\",\"done\":true}"))
					.andExpect(status().isBadRequest());
			verify(tasksMemory, never()).update(any());
		}

		@Test
		void formEncodedBodyReturnsUnsupportedMediaType() throws Exception {
			mock.perform(put(url).contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.param("title", "new")
					.param("done", "true"))
					.andExpect(status().isUnsupportedMediaType());
			verify(tasksMemory, never()).update(any());
		}
	}

	@Nested
	class deleteTaskById {
		String url = BASE_URL + "/" + dummyTask.id();

		@Test
		void existingTaskCorrectlyRemovedFromMemory() throws Exception {
			when(tasksMemory.remove(1)).thenReturn(dummyTask);
			mock.perform(delete(url)).andExpect(status().isNoContent());
		}

		@Test
		void nonExistingTaskDoesNothing() throws Exception {
			when(tasksMemory.remove(1)).thenReturn(null);
			mock.perform(delete(url)).andExpect(status().isNoContent());
		}
	}

	@Nested
	class getCount {
		String url = BASE_URL + "/count";

		@Test
		void returnsTotalAndDoneCounts() throws Exception {
			when(tasksMemory.count()).thenReturn(new TaskCount(5, 2));
			mock.perform(get(url)).andExpect(status().isOk())
					.andExpect(jsonPath("$.total").value(5))
					.andExpect(jsonPath("$.done").value(2));
			verify(tasksMemory).count();
		}

		@Test
		void returnsZeroCountsWhenNoTasks() throws Exception {
			when(tasksMemory.count()).thenReturn(new TaskCount(0, 0));
			mock.perform(get(url)).andExpect(status().isOk())
					.andExpect(jsonPath("$.total").value(0))
					.andExpect(jsonPath("$.done").value(0));
			verify(tasksMemory).count();
		}
	}
}
