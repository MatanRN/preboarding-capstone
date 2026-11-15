package com.example.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.example.tasks.records.Task;
import com.example.tasks.records.TaskCount;
import com.example.tasks.records.TaskCreateRequest;
import com.example.tasks.records.TaskUpdateRequest;

class TasksServiceTests {

	private TasksMemory tasksMemory;
	private TasksService tasksService;

	@BeforeEach
	void setUp() {
		tasksMemory = mock(TasksMemory.class);
		tasksService = new TasksService(tasksMemory);
	}

	@Nested
	class getAllTasks {

		@Test
		void nullDoneDelegatesToGetAll() {
			List<Task> expected = List.of(new Task(1, "Task 1", false));
			when(tasksMemory.getAll()).thenReturn(expected);

			List<Task> actual = tasksService.getAllTasks(null);

			assertEquals(expected, actual);
			verify(tasksMemory).getAll();
		}

		@Test
		void booleanDoneDelegatesToGetByDone() {
			List<Task> expected = List.of(new Task(2, "Task 2", true));
			when(tasksMemory.getByDone(true)).thenReturn(expected);

			List<Task> actual = tasksService.getAllTasks(true);

			assertEquals(expected, actual);
			verify(tasksMemory).getByDone(true);
		}
	}

	@Nested
	class getCount {

		@Test
		void delegatesToMemoryCount() {
			TaskCount expected = new TaskCount(5, 2);
			when(tasksMemory.count()).thenReturn(expected);

			TaskCount actual = tasksService.getCount();

			assertEquals(expected, actual);
			verify(tasksMemory).count();
		}
	}

	@Nested
	class getTaskById {

		@Test
		void existingIdReturnsPresentOptional() {
			Task task = new Task(1, "Task 1", false);
			when(tasksMemory.get(1)).thenReturn(task);

			Optional<Task> result = tasksService.getTaskById(1);

			assertTrue(result.isPresent());
			assertEquals(task, result.get());
			verify(tasksMemory).get(1);
		}

		@Test
		void missingIdReturnsEmptyOptional() {
			when(tasksMemory.get(99)).thenReturn(null);

			Optional<Task> result = tasksService.getTaskById(99);

			assertFalse(result.isPresent());
			verify(tasksMemory).get(99);
		}
	}

	@Nested
	class createTask {

		@Test
		void nullDoneDefaultsToFalse() {
			Task created = new Task(1, "Task 1", false);
			when(tasksMemory.create("Task 1", false)).thenReturn(created);

			Task result = tasksService.createTask(new TaskCreateRequest("Task 1", null));

			assertEquals(created, result);
			verify(tasksMemory).create("Task 1", false);
		}

		@Test
		void explicitDonePreserved() {
			Task created = new Task(1, "Task 1", true);
			when(tasksMemory.create("Task 1", true)).thenReturn(created);

			Task result = tasksService.createTask(new TaskCreateRequest("Task 1", true));

			assertEquals(created, result);
			verify(tasksMemory).create("Task 1", true);
		}
	}

	@Nested
	class updateTask {

		@Test
		void successfulUpdateReturnsPresentOptional() {
			Task previous = new Task(1, "old", false);
			Task updated = new Task(1, "new", true);
			when(tasksMemory.update(updated)).thenReturn(previous);

			Optional<Task> result = tasksService.updateTask(1, new TaskUpdateRequest("new", true));

			assertTrue(result.isPresent());
			assertEquals(updated, result.get());
			verify(tasksMemory).update(updated);
		}

		@Test
		void missingTaskReturnsEmptyOptional() {
			Task updated = new Task(99, "new", true);
			when(tasksMemory.update(updated)).thenReturn(null);

			Optional<Task> result = tasksService.updateTask(99, new TaskUpdateRequest("new", true));

			assertFalse(result.isPresent());
			verify(tasksMemory).update(updated);
		}
	}

	@Nested
	class deleteTask {

		@Test
		void delegatesToMemoryRemove() {
			tasksService.deleteTask(1);
			verify(tasksMemory).remove(1);
		}
	}
}
