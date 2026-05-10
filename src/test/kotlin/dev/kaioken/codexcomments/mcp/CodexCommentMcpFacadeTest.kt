package dev.kaioken.codexcomments.mcp

import dev.kaioken.codexcomments.comments.CodexCommentService
import dev.kaioken.codexcomments.comments.CommentAuthorType
import dev.kaioken.codexcomments.comments.ThreadStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class CodexCommentMcpFacadeTest {

    @Test
    fun `list threads maps service data into dto`() {
        val service = CodexCommentService()
        service.createLineThread(
            path = "src/App.kt",
            line = 12,
            body = "User note.",
            author = CommentAuthorType.USER,
        )
        val facade = CodexCommentMcpFacade(service)

        val result = facade.listThreads(status = null, path = null)

        assertEquals(1, result.items.size)
        assertEquals("src/App.kt", result.items.single().path)
        assertEquals(12, result.items.single().line)
        assertEquals("OPEN", result.items.single().status)
    }

    @Test
    fun `reply to comment appends codex message`() {
        val service = CodexCommentService()
        val thread = service.createFileThread(
            path = "src/Main.kt",
            body = "Please revise this.",
            author = CommentAuthorType.USER,
        )
        val facade = CodexCommentMcpFacade(service)

        val updated = facade.replyToComment(
            threadId = thread.id,
            body = "Updated and ready to verify.",
            author = "codex",
        )

        assertEquals(2, updated.messages.size)
        assertEquals("CODEX", updated.messages.last().author)
    }

    @Test
    fun `resolve comment flips thread status`() {
        val service = CodexCommentService()
        val thread = service.createFileThread(
            path = "src/Main.kt",
            body = "Please revise this.",
            author = CommentAuthorType.USER,
        )
        val facade = CodexCommentMcpFacade(service)

        val resolved = facade.resolveComment(thread.id)

        assertEquals("RESOLVED", resolved.status)
        assertEquals(ThreadStatus.RESOLVED, service.getThread(thread.id)?.status)
    }

    @Test
    fun `get thread returns complete thread dto`() {
        val service = CodexCommentService()
        val thread = service.createLineThread(
            path = "src/App.kt",
            line = 7,
            body = "Keep this small.",
            author = CommentAuthorType.USER,
        )
        val facade = CodexCommentMcpFacade(service)

        val loaded = facade.getThread(thread.id)

        assertNotNull(loaded)
        assertEquals(thread.id, loaded.id)
        assertEquals(1, loaded.messages.size)
    }

    @Test
    fun `missing thread id is rejected`() {
        val facade = CodexCommentMcpFacade(CodexCommentService())

        assertFailsWith<IllegalArgumentException> {
            facade.resolveComment("missing")
        }
    }
}
