package dev.kaioken.codexcomments.mcp

import dev.kaioken.codexcomments.comments.CodexCommentService
import dev.kaioken.codexcomments.comments.CommentAuthorType
import dev.kaioken.codexcomments.comments.CommentMessage
import dev.kaioken.codexcomments.comments.CommentThread
import dev.kaioken.codexcomments.comments.ThreadStatus

class CodexCommentMcpFacade(
    private val commentService: CodexCommentService,
) {
    fun listThreads(status: String?, path: String?): McpCommentThreadListDto {
        val resolvedStatus = status?.let(::parseStatus)
        return McpCommentThreadListDto(
            items = commentService.listThreads(resolvedStatus, path).map(::toDto),
        )
    }

    fun getThread(threadId: String): McpCommentThreadDto {
        val thread = commentService.getThread(threadId)
            ?: throw IllegalArgumentException("Thread not found: $threadId")
        return toDto(thread)
    }

    fun createFileComment(path: String, body: String, author: String = "codex"): McpCommentThreadDto {
        return toDto(commentService.createFileThread(path, body, parseAuthor(author)))
    }

    fun createLineComment(path: String, line: Int, body: String, author: String = "codex"): McpCommentThreadDto {
        return toDto(commentService.createLineThread(path, line, body, parseAuthor(author)))
    }

    fun replyToComment(threadId: String, body: String, author: String = "codex"): McpCommentThreadDto {
        return toDto(commentService.addReply(threadId, body, parseAuthor(author)))
    }

    fun resolveComment(threadId: String): McpCommentThreadDto {
        return toDto(commentService.resolveThread(threadId))
    }

    fun reopenComment(threadId: String): McpCommentThreadDto {
        return toDto(commentService.reopenThread(threadId))
    }

    private fun parseStatus(status: String): ThreadStatus {
        return when (status.trim().uppercase()) {
            "OPEN" -> ThreadStatus.OPEN
            "RESOLVED" -> ThreadStatus.RESOLVED
            else -> throw IllegalArgumentException("Unsupported status: $status")
        }
    }

    private fun parseAuthor(author: String): CommentAuthorType {
        return when (author.trim().uppercase()) {
            "USER" -> CommentAuthorType.USER
            "CODEX" -> CommentAuthorType.CODEX
            else -> throw IllegalArgumentException("Unsupported author: $author")
        }
    }

    private fun toDto(thread: CommentThread): McpCommentThreadDto {
        return McpCommentThreadDto(
            id = thread.id,
            path = thread.anchor.path,
            line = thread.anchor.line,
            status = thread.status.name,
            createdAt = thread.createdAt,
            updatedAt = thread.updatedAt,
            messages = thread.messages.map(::toDto),
        )
    }

    private fun toDto(message: CommentMessage): McpCommentMessageDto {
        return McpCommentMessageDto(
            id = message.id,
            author = message.authorType.name,
            body = message.body,
            createdAt = message.createdAt,
        )
    }
}
