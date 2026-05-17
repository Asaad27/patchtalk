package dev.kaioken.patchtalk.editor

import dev.kaioken.patchtalk.comments.CommentAnchor
import dev.kaioken.patchtalk.comments.CommentAuthorType
import dev.kaioken.patchtalk.comments.CommentMessage
import dev.kaioken.patchtalk.comments.CommentThread
import dev.kaioken.patchtalk.comments.ThreadStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PatchTalkCommentPresentationModelTest {

    @Test
    fun `open threads by line excludes resolved threads`() {
        val threadsByLine = PatchTalkCommentPresentationModel.openThreadsByLine(
            listOf(
                thread(id = "open", line = 12, status = ThreadStatus.OPEN, body = "Visible"),
                thread(id = "resolved", line = 12, status = ThreadStatus.RESOLVED, body = "Hidden"),
            ),
        )

        assertEquals(listOf("open"), threadsByLine[12]?.map { it.id })
    }

    @Test
    fun `open threads by line skips file comments and resolved-only lines`() {
        val threadsByLine = PatchTalkCommentPresentationModel.openThreadsByLine(
            listOf(
                thread(id = "file", line = null, status = ThreadStatus.OPEN, body = "File-level"),
                thread(id = "resolved", line = 30, status = ThreadStatus.RESOLVED, body = "Resolved"),
            ),
        )

        assertTrue(threadsByLine.isEmpty())
    }

    private fun thread(id: String, line: Int?, status: ThreadStatus, body: String): CommentThread {
        return CommentThread(
            id = id,
            anchor = CommentAnchor(path = "src/App.kt", line = line),
            status = status,
            messages = mutableListOf(
                CommentMessage(
                    id = "$id-message",
                    authorType = CommentAuthorType.USER,
                    body = body,
                    createdAt = "2026-05-17T10:00:00Z",
                ),
            ),
        )
    }
}
