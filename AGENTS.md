# Purpose
This is a Java 17, SpringBoot 4 REST API for processing tasks, which are Records with an id, title and done fields.

# Commands
- Test: ./mvnw test
- Run: ./mvnw spring-boot:run 

# Rules and conventions
- Testing:
    - All tests of a single controller should exist in a single file, all using MockMvc.
    - All tests of a single endpoint should live under a @Nested class.
    - Each endpoint should always have at least one test.
- General:
    - Maintain the single responsibility principle: A @RestController handle HTTP, a @Service handles business logic.