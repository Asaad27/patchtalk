# PatchTalk

PatchTalk is a JetBrains plugin for local review-style comment threads inside the IDE.

It lets you add file-level and line-level comments without opening a pull request, keeps those threads visible in the editor gutter and a dedicated tool window, and exposes them to MCP-compatible coding agents such as Codex.

## Features

- Add local file and line comments from JetBrains editor and project context menus
- Show comment markers in the line-number gutter
- Review threads in a dedicated `PatchTalk` tool window
- Reply, resolve, and reopen threads inside the IDE
- Read and manage threads through JetBrains MCP tools
- Persist thread state per project under `.idea/codex-comments.xml`

## Requirements

- A JetBrains IDE on `2026.1.x`
- The bundled JetBrains `MCP Server` plugin enabled in the IDE

## Installation

### JetBrains Marketplace

Install `PatchTalk` from the JetBrains Marketplace once the public listing is approved.

### Install from Disk

1. Build the plugin ZIP with `buildPlugin`.
2. Open `Settings | Plugins`.
3. Use the gear menu and choose `Install Plugin from Disk`.
4. Select the ZIP from `build/distributions/`.

## MCP Workflow

After installing PatchTalk:

1. Open `Settings | Tools | MCP Server` in the IDE.
2. Enable the built-in JetBrains MCP server.
3. Connect your MCP client to the JetBrains server.

PatchTalk exposes these tools through JetBrains MCP:

- `list_comment_threads`
- `get_comment_thread`
- `create_file_comment`
- `create_line_comment`
- `reply_to_comment`
- `resolve_comment`
- `reopen_comment`

## Development

Common local verification:

- `./gradlew --no-daemon test`
- `./gradlew --no-daemon check`
- `./gradlew --no-daemon verifyPluginProjectConfiguration`
- `./gradlew --no-daemon verifyPluginStructure`
- `./gradlew --no-daemon buildPlugin`

## Publishing

PatchTalk is set up for Marketplace publishing with environment variables:

- `PUBLISH_TOKEN`
- `CERTIFICATE_CHAIN`
- `PRIVATE_KEY`
- `PRIVATE_KEY_PASSWORD`

Once those are set, Gradle can sign and publish the plugin.

## License

PatchTalk is released under the MIT License. See [LICENSE](LICENSE).
