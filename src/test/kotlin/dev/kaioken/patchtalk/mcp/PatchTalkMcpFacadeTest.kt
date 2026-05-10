package dev.kaioken.patchtalk.mcp

import dev.kaioken.patchtalk.comments.PatchTalkCommentService
import dev.kaioken.patchtalk.comments.CommentAuthorType
import dev.kaioken.patchtalk.comments.ThreadStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class PatchTalkMcpFacadeTest {

    @Test
    fun `list threads maps service data into dto`() {
        val service = PatchTalkCommentService()
        service.createLineThread(
            path = "src/App.kt",
            line = 12,
            body = "User note.",
            author = CommentAuthorType.USER,
        )
        val facade = PatchTalkMcpFacade(service)

        val result = facade.listThreads(status = null, path = null)

        assertEquals(1, result.items.size)
        assertEquals("src/App.kt", result.items.single().path)
        assertEquals(12, result.items.single().line)
        assertEquals("OPEN", result.items.single().status)
    }

    @Test
    fun `reply to comment appends agent message`() {
        val service = PatchTalkCommentService()
        val thread = service.createFileThread(
            path = "src/Main.kt",
            body = "Please revise this.",
            author = CommentAuthorType.USER,
        )
        val facade = PatchTalkMcpFacade(service)

        val updated = facade.replyToComment(
            threadId = thread.id,
            body = "Updated and ready to verify.",
            author = "agent",
        )

        assertEquals(2, updated.messages.size)
        assertEquals("AGENT", updated.messages.last().author)
    }

    @Test
    fun `resolve comment flips thread status`() {
        val service = PatchTalkCommentService()
        val thread = service.createFileThread(
            path = "src/Main.kt",
            body = "Please revise this.",
            author = CommentAuthorType.USER,
        )
        val facade = PatchTalkMcpFacade(service)

        val resolved = facade.resolveComment(thread.id)

        assertEquals("RESOLVED", resolved.status)
        assertEquals(ThreadStatus.RESOLVED, service.getThread(thread.id)?.status)
    }

    @Test
    fun `get thread returns complete thread dto`() {
        val service = PatchTalkCommentService()
        val thread = service.createLineThread(
            path = "src/App.kt",
            line = 7,
            body = "Keep this small.",
            author = CommentAuthorType.USER,
        )
        val facade = PatchTalkMcpFacade(service)

        val loaded = facade.getThread(thread.id)

        assertNotNull(loaded)
        assertEquals(thread.id, loaded.id)
        assertEquals(1, loaded.messages.size)
    }

    @Test
    fun `missing thread id is rejected`() {
        val facade = PatchTalkMcpFacade(PatchTalkCommentService())

        assertFailsWith<IllegalArgumentException> {
            facade.resolveComment("missing")
        }
    }
}
