package dev.kaioken.codexcomments.editor

import com.intellij.openapi.editor.markup.GutterIconRenderer
import dev.kaioken.codexcomments.comments.CommentAnchor
import dev.kaioken.codexcomments.comments.CommentAuthorType
import dev.kaioken.codexcomments.comments.CommentMessage
import dev.kaioken.codexcomments.comments.CommentThread
import dev.kaioken.codexcomments.comments.ThreadStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CodexCommentGutterRendererTest {

    @Test
    fun `gutter renderer aligns comment icon with line comment area`() {
        val renderer = CodexCommentGutterRenderer(listOf(thread("Refactor this branch."))) {}

        assertEquals(GutterIconRenderer.Alignment.LINE_NUMBERS, renderer.alignment)
    }

    @Test
    fun `gutter renderer tooltip shows latest preview`() {
        val renderer = CodexCommentGutterRenderer(
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
