package dev.kaioken.codexcomments.toolwindow

import dev.kaioken.codexcomments.comments.CommentAnchor
import dev.kaioken.codexcomments.comments.CommentAuthorType
import dev.kaioken.codexcomments.comments.CommentMessage
import dev.kaioken.codexcomments.comments.CommentThread
import dev.kaioken.codexcomments.comments.ThreadStatus
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class CodexCommentThreadPresentationTest {

    @Test
    fun `list item presentation favors file name and line over full path`() {
        val presentation = CodexCommentThreadPresentation.listItem(
            thread(
                path = "src/main/kotlin/dev/kaioken/codexcomments/toolwindow/CodexCommentsPanel.kt",
                line = 74,
                status = ThreadStatus.OPEN,
                messageBodies = listOf("This panel needs a cleaner hierarchy."),
            ),
        )

        assertEquals("CodexCommentsPanel.kt", presentation.title)
        assertEquals("Line 74", presentation.location)
        assertEquals("Open", presentation.statusLabel)
        assertEquals("This panel needs a cleaner hierarchy.", presentation.preview)
    }

    @Test
    fun `detail html renders badges author labels and escaped body`() {
        val html = CodexCommentThreadPresentation.detailHtml(
            thread(
                path = "src/App.kt",
                line = 18,
                status = ThreadStatus.RESOLVED,
                messageBodies = listOf("Use <strong>less</strong> chrome.", "Codex fixed it."),
                authorTypes = listOf(CommentAuthorType.USER, CommentAuthorType.CODEX),
            ),
        )

        assertContains(html, "Resolved")
        assertContains(html, "App.kt")
        assertContains(html, "Line 18")
        assertContains(html, "You")
        assertContains(html, "Codex")
        assertContains(html, "Use &lt;strong&gt;less&lt;/strong&gt; chrome.")
    }

    private fun thread(
        path: String,
        line: Int?,
        status: ThreadStatus,
        messageBodies: List<String>,
        authorTypes: List<CommentAuthorType> = List(messageBodies.size) { CommentAuthorType.USER },
    ): CommentThread {
        return CommentThread(
            id = "thread-1",
            anchor = CommentAnchor(path = path, line = line),
            status = status,
            messages = messageBodies.mapIndexed { index, body ->
                CommentMessage(
                    id = "message-$index",
                    authorType = authorTypes[index],
                    body = body,
                    createdAt = "2026-05-10T12:00:0${index}Z",
                )
            }.toMutableList(),
            createdAt = "2026-05-10T12:00:00Z",
            updatedAt = "2026-05-10T12:05:00Z",
        )
    }
}
