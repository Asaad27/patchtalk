package dev.kaioken.codexcomments.comments

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class CodexCommentServiceTest {

    @Test
    fun `create file thread stores open thread with initial message`() {
        val service = CodexCommentService()

        val thread = service.createFileThread(
            path = "src/Main.kt",
            body = "Please rename this file entry point.",
            author = CommentAuthorType.USER,
        )

        assertEquals(ThreadStatus.OPEN, thread.status)
        assertEquals("src/Main.kt", thread.anchor.path)
        assertEquals(null, thread.anchor.line)
        assertEquals(1, thread.messages.size)
        assertEquals(CommentAuthorType.USER, thread.messages.single().authorType)
    }

    @Test
    fun `create line thread stores line anchor`() {
        val service = CodexCommentService()

        val thread = service.createLineThread(
            path = "src/App.kt",
            line = 18,
            body = "This branch needs simplification.",
            author = CommentAuthorType.USER,
        )

        assertEquals("src/App.kt", thread.anchor.path)
        assertEquals(18, thread.anchor.line)
    }

    @Test
    fun `reply appends message to existing thread`() {
        val service = CodexCommentService()
        val thread = service.createLineThread(
            path = "src/App.kt",
            line = 18,
            body = "This branch needs simplification.",
            author = CommentAuthorType.USER,
        )

        val updated = service.addReply(
            threadId = thread.id,
            body = "I will refactor it.",
            author = CommentAuthorType.CODEX,
        )

        assertEquals(2, updated.messages.size)
        assertEquals(CommentAuthorType.CODEX, updated.messages.last().authorType)
        assertEquals("I will refactor it.", updated.messages.last().body)
    }

    @Test
    fun `resolve and reopen change thread status`() {
        val service = CodexCommentService()
        val thread = service.createFileThread(
            path = "src/Main.kt",
            body = "Track this for follow-up.",
            author = CommentAuthorType.USER,
        )

        val resolved = service.resolveThread(thread.id)
        val reopened = service.reopenThread(thread.id)

        assertEquals(ThreadStatus.RESOLVED, resolved.status)
        assertEquals(ThreadStatus.OPEN, reopened.status)
    }

    @Test
    fun `list threads can filter by status and path`() {
        val service = CodexCommentService()

        val first = service.createFileThread(
            path = "src/Main.kt",
            body = "Main file note.",
            author = CommentAuthorType.USER,
        )
        service.createLineThread(
            path = "src/Other.kt",
            line = 4,
            body = "Other file note.",
            author = CommentAuthorType.USER,
        )
        service.resolveThread(first.id)

        val resolvedThreads = service.listThreads(status = ThreadStatus.RESOLVED)
        val pathThreads = service.listThreads(path = "src/Other.kt")

        assertEquals(listOf(first.id), resolvedThreads.map { it.id })
        assertEquals(listOf("src/Other.kt"), pathThreads.map { it.anchor.path })
    }

    @Test
    fun `empty body and invalid line are rejected`() {
        val service = CodexCommentService()

        assertFailsWith<IllegalArgumentException> {
            service.createFileThread(
                path = "src/Main.kt",
                body = "   ",
                author = CommentAuthorType.USER,
            )
        }

        assertFailsWith<IllegalArgumentException> {
            service.createLineThread(
                path = "src/Main.kt",
                line = 0,
                body = "Bad line anchor.",
                author = CommentAuthorType.USER,
            )
        }
    }

    @Test
    fun `get thread returns stored thread`() {
        val service = CodexCommentService()
        val created = service.createFileThread(
            path = "src/Main.kt",
            body = "Keep an eye on this.",
            author = CommentAuthorType.USER,
        )

        val loaded = service.getThread(created.id)

        assertNotNull(loaded)
        assertEquals(created.id, loaded.id)
    }
}
