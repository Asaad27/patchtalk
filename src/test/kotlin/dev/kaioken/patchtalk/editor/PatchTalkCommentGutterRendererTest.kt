package dev.kaioken.patchtalk.editor

import com.intellij.openapi.editor.markup.GutterIconRenderer
import dev.kaioken.patchtalk.comments.CommentAnchor
import dev.kaioken.patchtalk.comments.CommentAuthorType
import dev.kaioken.patchtalk.comments.CommentMessage
import dev.kaioken.patchtalk.comments.CommentThread
import dev.kaioken.patchtalk.comments.ThreadStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PatchTalkCommentGutterRendererTest {

    @Test
    fun `gutter renderer uses supported gutter strip alignment`() {
        val renderer = PatchTalkCommentGutterRenderer(listOf(thread("Refactor this branch."))) {}

        assertEquals(GutterIconRenderer.Alignment.LEFT, renderer.alignment)
    }

    @Test
    fun `gutter renderer tooltip shows latest preview`() {
        val renderer = PatchTalkCommentGutterRenderer(
            listOf(thread("Original note.", "Latest note.")),
        ) {}

        assertTrue(renderer.tooltipText.contains("Latest note."))
    }

    private fun thread(vararg bodies: String): CommentThread {
        return CommentThread(
            id = "thread-1",
            anchor = CommentAnchor(path = "src/App.kt", line = 12),
            status = ThreadStatus.OPEN,
            messages = bodies.mapIndexed { index, body ->
                CommentMessage(
                    id = "message-$index",
                    authorType = CommentAuthorType.USER,
                    body = body,
                    createdAt = "2026-05-10T12:00:0${index}Z",
                )
            }.toMutableList(),
        )
    }
}
