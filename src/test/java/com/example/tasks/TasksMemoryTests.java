package com.example.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TasksMemoryTests {

	private TasksMemory memory;

	@BeforeEach
	void setUp() {
		memory = new TasksMemory();
	}

	@Nested
	class getByDone {

		@BeforeEach
		void seed() {
			memory.add(new Task(1, "a", false));
			memory.add(new Task(2, "b", true));
			memory.add(new Task(3, "c", false));
		}

		@Test
		void trueReturnsOnlyDoneTasks() {
			List<Task> result = memory.getByDone(true);
			assertEquals(List.of(new Task(2, "b", true)), result);
		}

		@Test
		void falseReturnsOnlyUndoneTasks() {
			List<Task> result = memory.getByDone(false);
			assertEquals(2, result.size());
			assertTrue(result.stream().noneMatch(Task::done));
		}
	}

	@Test
	void getByDoneOnEmptyStoreReturnsEmptyList() {
		assertTrue(memory.getByDone(true).isEmpty());
		assertTrue(memory.getByDone(false).isEmpty());
	}

	@Nested
	class update {

		@Test
		void existingIdReplacesTaskAndReturnsPrevious() {
			Task original = new Task(1, "old", false);
			memory.add(original);
			Task updated = new Task(1, "new", true);

			assertEquals(original, memory.update(updated));
			assertEquals(updated, memory.get(1));
		}

		@Test
		void nonExistingIdReturnsNullAndDoesNotAdd() {
			assertNull(memory.update(new Task(42, "ghost", false)));
			assertNull(memory.get(42));
		}
	}
}
