package com.example.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.example.tasks.records.Task;
import com.example.tasks.records.TaskCount;

class TasksMemoryTests {

	private TasksMemory memory;

	@BeforeEach
	void setUp() {
		memory = new TasksMemory();
	}

	@Nested
	class create {

		@Test
		void generatesSequentialIds() {
			Task first = memory.create("first", false);
			Task second = memory.create("second", true);

			assertEquals(1L, first.id());
			assertEquals("first", first.title());
			assertEquals(false, first.done());

			assertEquals(2L, second.id());
			assertEquals("second", second.title());
			assertEquals(true, second.done());

			assertEquals(first, memory.get(1L));
			assertEquals(second, memory.get(2L));
			assertEquals(new TaskCount(2, 1), memory.count());
		}

		@Test
		void advancesCounterBeyondManuallyAddedIds() {
			memory.add(new Task(10, "manual", false));
			Task next = memory.create("after manual", true);

			assertEquals(11L, next.id());
			assertEquals("after manual", next.title());
			assertEquals(new TaskCount(2, 1), memory.count());
		}
	}

	@Nested
	class getAll {

		@Test
		void returnsAllTasks() {
			memory.add(new Task(1, "a", false));
			memory.add(new Task(2, "b", true));
			assertEquals(2, memory.getAll().size());
		}

		@Test
		void returnsEmptyListWhenEmpty() {
			assertTrue(memory.getAll().isEmpty());
		}
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

	@Nested
	class count {

		@Test
		void emptyStoreReturnsZeroCounts() {
			TaskCount result = memory.count();
			assertEquals(new TaskCount(0, 0), result);
		}

		@Test
		void returnsTotalAndDoneCounts() {
			memory.add(new Task(1, "task1", true));
			memory.add(new Task(2, "task2", false));
			memory.add(new Task(3, "task3", true));

			TaskCount result = memory.count();
			assertEquals(new TaskCount(3, 2), result);
		}

		@Test
		void reflectsUpdatesAndDeletions() {
			memory.add(new Task(1, "task1", false));
			memory.add(new Task(2, "task2", true));

			assertEquals(new TaskCount(2, 1), memory.count());

			memory.update(new Task(1, "task1", true));
			assertEquals(new TaskCount(2, 2), memory.count());

			memory.update(new Task(1, "task1", false));
			assertEquals(new TaskCount(2, 1), memory.count());

			// updating non-existent id changes nothing
			memory.update(new Task(99, "ghost", true));
			assertEquals(new TaskCount(2, 1), memory.count());

			// removing non-existent id changes nothing
			memory.remove(99);
			assertEquals(new TaskCount(2, 1), memory.count());

			memory.remove(2);
			assertEquals(new TaskCount(1, 0), memory.count());

			memory.remove(1);
			assertEquals(new TaskCount(0, 0), memory.count());
		}

		@Test
		void addOverwritingExistingTaskMaintainsCounts() {
			memory.add(new Task(1, "task1", false));
			assertEquals(new TaskCount(1, 0), memory.count());

			// overwrite with done=true (total stays 1, done becomes 1)
			memory.add(new Task(1, "task1-updated", true));
			assertEquals(new TaskCount(1, 1), memory.count());

			// overwrite with done=false (total stays 1, done becomes 0)
			memory.add(new Task(1, "task1-updated-again", false));
			assertEquals(new TaskCount(1, 0), memory.count());

			// overwrite with same done status (counts unchanged)
			memory.add(new Task(1, "task1-final", false));
			assertEquals(new TaskCount(1, 0), memory.count());
		}

		@Test
		void neverReturnsTornCountsUnderConcurrentWrites() throws Exception {
			int writers = 4;
			int iterations = 20_000;
			ExecutorService pool = Executors.newFixedThreadPool(writers + 1);
			AtomicBoolean writing = new AtomicBoolean(true);
			try {
				List<Future<?>> writerJobs = new ArrayList<>();
				for (int w = 0; w < writers; w++) {
					long id = w;
					writerJobs.add(pool.submit(() -> {
						for (int i = 0; i < iterations; i++) {
							memory.add(new Task(id, "t", true));
							memory.remove(id);
						}
					}));
				}
				// Throws IllegalArgumentException if count() ever sees done > total.
				Future<?> reader = pool.submit(() -> {
					while (writing.get()) {
						memory.count();
					}
				});
				for (Future<?> job : writerJobs) {
					job.get(30, TimeUnit.SECONDS);
				}
				writing.set(false);
				reader.get(30, TimeUnit.SECONDS);
			} finally {
				writing.set(false);
				pool.shutdownNow();
			}
			assertEquals(new TaskCount(0, 0), memory.count());
		}
	}
}
