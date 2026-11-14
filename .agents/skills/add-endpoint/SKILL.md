---
name: add-endpoint
description: Adds a new REST endpoint to this Spring Boot project with tests. Use when asked to add or change an API endpoint.
---

# Add an endpoint

1. Read the existing controller and tests to match conventions.
2. Write a MockMvc test suite for the new endpoint first, including error cases. Only test behavior the code actually owns.
4. Implement the endpoint. Keep business logic out of the controller.
5. Run ./mvnw test and fix failures until all pass.
6. Summarize the change and list the files you touched. Explain each test in one line - what bug it would catch. 
7. Suggest a commit message based on the existing git repository convention