# JetBrains Codex Comment Plugin Design

## Goal

Build a JetBrains plugin that lets a user create local review-style comment threads in the IDE outside pull requests, then lets Codex read, reply to, and resolve those threads.

## Approved Product Decisions

- Comment anchoring: `line-and-file`
- IDE surfaces: `editor + diff viewer + comment tool window`
- Storage: `project-local in .idea`
- Codex authority: `reply + resolve`

## User Experience

The plugin supports two thread types:

- `line thread`: attached to a project-relative file path and one 1-based line number
- `file thread`: attached to a project-relative file path without a line

Threads appear in three places:

1. Normal editors
   - User adds a line comment from the editor popup action.
   - Existing line comments show a gutter icon on the anchored line.
   - Clicking the gutter icon opens the main comment tool window and selects that thread.

2. Diff viewers
   - The same gutter icon rendering is applied to any editor instance that can be mapped back to a project file.
   - The same editor popup action is available when the diff editor exposes a project file and caret line.

3. Comment tool window
   - Lists all threads for the open project.
   - Supports open/resolved filtering.
   - Shows thread details, full message history, reply box, and resolve/reopen controls.
   - Can navigate back to the source file and line.

## Data Model

### Thread

- `id`: stable unique thread id
- `anchor`:
  - `path`: project-relative file path
  - `line`: optional 1-based line number
- `status`: `OPEN` or `RESOLVED`
- `messages`: ordered list of thread messages
- `createdAt`, `updatedAt`

### Message

- `id`: stable unique message id
- `authorType`: `USER` or `CODEX`
- `body`
- `createdAt`

## Persistence

Comments are stored in a project-local XML state file under `.idea/codex-comments.xml`.

That choice gives the plugin a durable project-scoped store without polluting source files. It also makes the state readable by Codex through normal filesystem access if MCP is unavailable, while still letting the JetBrains plugin own the UI.

## Core Services

### Comment service

Project-level service responsible for:

- loading and saving state
- creating threads
- adding replies
- resolving and reopening threads
- listing and filtering threads
- publishing change notifications to UI integrations

### Editor presentation service

Project-level service responsible for:

- tracking open editors
- rendering gutter markers for anchored line comments
- refreshing markers after comment changes or document line shifts
- opening the tool window focused on a selected thread

### Tool window model

Thin UI model around the comment service that exposes:

- filtered thread list
- selected thread
- reply/resolve actions
- navigation back to source

## MCP Integration

The plugin should expose an MCP toolset when the JetBrains built-in `com.intellij.mcpServer` plugin is present.

Planned tools:

- `list_comment_threads(status?, path?)`
- `get_comment_thread(threadId)`
- `create_file_comment(path, body, author = "codex")`
- `create_line_comment(path, line, body, author = "codex")`
- `reply_to_comment(threadId, body, author = "codex")`
- `resolve_comment(threadId)`
- `reopen_comment(threadId)`

All tool outputs return structured serializable DTOs so Codex can parse them reliably.

## Diff Strategy

The first version does not implement a custom diff-only comment backend. Instead, it reuses the same file/line thread model everywhere and renders it onto editor instances that can be associated with project files. This keeps the feature coherent between normal editors and diffs.

## Error Handling

- Reject comment creation when no project file can be resolved.
- Reject line comments for non-positive line numbers.
- Reject empty thread bodies or replies.
- Reject MCP thread operations for missing thread ids with explicit error text.
- Ignore stale editor mappings gracefully instead of failing the whole refresh.

## Testing Strategy

Automated coverage should focus on the durable behavior, not the Swing shell:

- comment service lifecycle:
  - create file thread
  - create line thread
  - reply
  - resolve/reopen
  - filter by status and path
- persistence round-trip:
  - state serialize/load with multiple threads
- line anchor tracking:
  - editor document edits update stored line anchors for open files
- MCP-facing facade:
  - list/get/reply/resolve semantics return expected DTOs

UI code should stay thin enough that the core behavior is validated through service-level tests.

## Constraints

- No source-code comments are inserted into project files.
- No PR or VCS dependency is required.
- Codex actions on comments only affect plugin-owned thread state unless the user separately asks Codex to edit code.
