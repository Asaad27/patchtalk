@file:Suppress("unused", "FunctionName")

package dev.kaioken.patchtalk.mcp

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import com.intellij.mcpserver.project
import com.intellij.openapi.components.service
import kotlinx.coroutines.currentCoroutineContext

class PatchTalkToolset : McpToolset {
    @McpTool(name = "list_comment_threads")
    @McpDescription("Lists JetBrains PatchTalk comment threads for the current project.")
    suspend fun listCommentThreads(
        @McpDescription("Optional status filter: OPEN or RESOLVED.") status: String? = null,
        @McpDescription("Optional project-relative path filter.") path: String? = null,
    ): McpCommentThreadListDto {
        return facade().listThreads(status, path)
    }

    @McpTool(name = "get_comment_thread")
    @McpDescription("Returns one JetBrains PatchTalk comment thread by id.")
    suspend fun getCommentThread(
        @McpDescription("Thread id to load.") threadId: String,
    ): McpCommentThreadDto {
        return facade().getThread(threadId)
    }

    @McpTool(name = "create_file_comment")
    @McpDescription("Creates a file-level JetBrains PatchTalk comment thread.")
    suspend fun createFileComment(
        @McpDescription("Project-relative file path.") path: String,
        @McpDescription("Initial thread body.") body: String,
        @McpDescription("Author name, either user or agent.") author: String = "agent",
    ): McpCommentThreadDto {
        return facade().createFileComment(path, body, author)
    }

    @McpTool(name = "create_line_comment")
    @McpDescription("Creates a line-level JetBrains PatchTalk comment thread.")
    suspend fun createLineComment(
        @McpDescription("Project-relative file path.") path: String,
        @McpDescription("1-based line number.") line: Int,
        @McpDescription("Initial thread body.") body: String,
        @McpDescription("Author name, either user or agent.") author: String = "agent",
    ): McpCommentThreadDto {
        return facade().createLineComment(path, line, body, author)
    }

    @McpTool(name = "reply_to_comment")
    @McpDescription("Adds a reply to an existing JetBrains PatchTalk comment thread.")
    suspend fun replyToComment(
        @McpDescription("Thread id to reply to.") threadId: String,
        @McpDescription("Reply body.") body: String,
        @McpDescription("Author name, either user or agent.") author: String = "agent",
    ): McpCommentThreadDto {
        return facade().replyToComment(threadId, body, author)
    }

    @McpTool(name = "resolve_comment")
    @McpDescription("Marks a JetBrains PatchTalk comment thread as resolved.")
    suspend fun resolveComment(
        @McpDescription("Thread id to resolve.") threadId: String,
    ): McpCommentThreadDto {
        return facade().resolveComment(threadId)
    }

    @McpTool(name = "reopen_comment")
    @McpDescription("Reopens a resolved JetBrains PatchTalk comment thread.")
    suspend fun reopenComment(
        @McpDescription("Thread id to reopen.") threadId: String,
    ): McpCommentThreadDto {
        return facade().reopenComment(threadId)
    }

    private suspend fun facade(): PatchTalkMcpFacade {
        val project = currentCoroutineContext().project
        return PatchTalkMcpFacade(project.service())
    }
}
