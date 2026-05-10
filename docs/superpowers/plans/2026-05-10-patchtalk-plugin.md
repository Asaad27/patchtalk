# PatchTalk Plugin Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Kotlin IntelliJ plugin that supports local review-style file and line comments in the IDE, persists them under `.idea`, and exposes them to PatchTalk through JetBrains MCP tools.

**Architecture:** The implementation centers on a project service that owns thread state and persistence, plus thin editor and tool window integrations that project the same thread model into the IDE UI. MCP support is implemented as a small toolset wrapper over the same service so all comment state stays consistent.

**Tech Stack:** Kotlin, Gradle, IntelliJ Platform Plugin SDK, JetBrains built-in MCP plugin, JUnit/platform tests, Swing UI

---

### Task 1: Scaffold Plugin Project

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `src/main/resources/META-INF/plugin.xml`
- Create: `src/main/resources/icons/patchTalkComment.svg`
- Create: `src/main/kotlin/dev/kaioken/codexcomments/PluginIds.kt`

- [ ] **Step 1: Create Gradle/plugin metadata files**

- [ ] **Step 2: Generate Gradle wrapper**

Run: `gradle wrapper --gradle-version 8.10.2`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Run baseline Gradle task**

Run: `.\gradlew.bat --no-daemon tasks`
Expected: `BUILD SUCCESSFUL`

### Task 2: Build Comment Domain With Tests

**Files:**
- Create: `src/test/kotlin/dev/kaioken/codexcomments/comments/PatchTalkCommentServiceTest.kt`
- Create: `src/main/kotlin/dev/kaioken/codexcomments/comments/PatchTalkCommentModels.kt`
- Create: `src/main/kotlin/dev/kaioken/codexcomments/comments/PatchTalkCommentProjectState.kt`
- Create: `src/main/kotlin/dev/kaioken/codexcomments/comments/PatchTalkCommentEvents.kt`
- Create: `src/main/kotlin/dev/kaioken/codexcomments/comments/PatchTalkCommentService.kt`

- [ ] **Step 1: Write failing service tests for thread lifecycle**

- [ ] **Step 2: Run the targeted test task and confirm failure**

Run: `.\gradlew.bat --no-daemon test --tests dev.kaioken.patchtalk.comments.PatchTalkCommentServiceTest`
Expected: test failure because comment domain/service classes do not exist yet

- [ ] **Step 3: Implement minimal serializable models and service**

- [ ] **Step 4: Re-run the targeted service tests**

Run: `.\gradlew.bat --no-daemon test --tests dev.kaioken.patchtalk.comments.PatchTalkCommentServiceTest`
Expected: `BUILD SUCCESSFUL`

### Task 3: Add Line Anchor Tracking With Tests

**Files:**
- Create: `src/test/kotlin/dev/kaioken/codexcomments/comments/PatchTalkCommentLineTrackerTest.kt`
- Create: `src/main/kotlin/dev/kaioken/codexcomments/comments/PatchTalkCommentLineTracker.kt`
- Modify: `src/main/kotlin/dev/kaioken/codexcomments/comments/PatchTalkCommentService.kt`

- [ ] **Step 1: Write failing tests for line tracking after document edits**

- [ ] **Step 2: Run the targeted test task and confirm failure**

Run: `.\gradlew.bat --no-daemon test --tests dev.kaioken.patchtalk.comments.PatchTalkCommentLineTrackerTest`
Expected: test failure because line tracker behavior is missing

- [ ] **Step 3: Implement minimal line tracking integration**

- [ ] **Step 4: Re-run the targeted line tracking tests**

Run: `.\gradlew.bat --no-daemon test --tests dev.kaioken.patchtalk.comments.PatchTalkCommentLineTrackerTest`
Expected: `BUILD SUCCESSFUL`

### Task 4: Add IDE Actions, Gutter Rendering, and Tool Window

**Files:**
- Create: `src/main/kotlin/dev/kaioken/codexcomments/actions/AddLineCommentAction.kt`
- Create: `src/main/kotlin/dev/kaioken/codexcomments/actions/AddFileCommentAction.kt`
- Create: `src/main/kotlin/dev/kaioken/codexcomments/editor/PatchTalkCommentEditorPresentationService.kt`
- Create: `src/main/kotlin/dev/kaioken/codexcomments/editor/PatchTalkCommentGutterRenderer.kt`
- Create: `src/main/kotlin/dev/kaioken/codexcomments/toolwindow/PatchTalkToolWindowFactory.kt`
- Create: `src/main/kotlin/dev/kaioken/codexcomments/toolwindow/PatchTalkPanel.kt`
- Modify: `src/main/resources/META-INF/plugin.xml`

- [ ] **Step 1: Add a thin UI integration around the service**

- [ ] **Step 2: Run a build compile check**

Run: `.\gradlew.bat --no-daemon compileKotlin`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Refine any compile issues from IntelliJ API wiring**

- [ ] **Step 4: Re-run compile**

Run: `.\gradlew.bat --no-daemon compileKotlin`
Expected: `BUILD SUCCESSFUL`

### Task 5: Add MCP Toolset With Tests

**Files:**
- Create: `src/test/kotlin/dev/kaioken/codexcomments/mcp/PatchTalkMcpFacadeTest.kt`
- Create: `src/main/kotlin/dev/kaioken/codexcomments/mcp/PatchTalkMcpModels.kt`
- Create: `src/main/kotlin/dev/kaioken/codexcomments/mcp/PatchTalkMcpFacade.kt`
- Create: `src/main/kotlin/dev/kaioken/codexcomments/mcp/PatchTalkToolset.kt`
- Modify: `src/main/resources/META-INF/plugin.xml`

- [ ] **Step 1: Write failing tests for MCP-facing list/get/reply/resolve behavior**

- [ ] **Step 2: Run the targeted MCP test task and confirm failure**

Run: `.\gradlew.bat --no-daemon test --tests dev.kaioken.patchtalk.mcp.PatchTalkMcpFacadeTest`
Expected: test failure because the MCP facade does not exist yet

- [ ] **Step 3: Implement facade DTOs and the JetBrains MCP toolset wrapper**

- [ ] **Step 4: Re-run the targeted MCP tests**

Run: `.\gradlew.bat --no-daemon test --tests dev.kaioken.patchtalk.mcp.PatchTalkMcpFacadeTest`
Expected: `BUILD SUCCESSFUL`

### Task 6: Final Verification

**Files:**
- Verify only

- [ ] **Step 1: Run local tests**

Run: `.\gradlew.bat --no-daemon test`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 2: Run project checks**

Run: `.\gradlew.bat --no-daemon check`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Smoke-test plugin packaging**

Run: `.\gradlew.bat --no-daemon buildPlugin`
Expected: `BUILD SUCCESSFUL`
