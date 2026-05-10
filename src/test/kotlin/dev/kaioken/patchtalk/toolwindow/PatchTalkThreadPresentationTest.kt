package dev.kaioken.patchtalk.toolwindow

import dev.kaioken.patchtalk.comments.CommentAnchor
import dev.kaioken.patchtalk.comments.CommentAuthorType
import dev.kaioken.patchtalk.comments.CommentMessage
import dev.kaioken.patchtalk.comments.CommentThread
import dev.kaioken.patchtalk.comments.ThreadStatus
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class PatchTalkThreadPresentationTest {

    @Test
    fun `list item presentation favors file name and line over full path`() {
        val presentation = PatchTalkThreadPresentation.listItem(
            thread(
                path = "src/main/kotlin/dev/kaioken/patchtalk/toolwindow/PatchTalkPanel.kt",
                line = 74,
                status = ThreadStatus.OPEN,
                messageBodies = listOf("This panel needs a cleaner hierarchy."),
            ),
        )

        assertEquals("PatchTalkPanel.kt", presentation.title)
        assertEquals("Line 74", presentation.location)
        assertEquals("Open", presentation.statusLabel)
        assertEquals("This panel needs a cleaner hierarchy.", presentation.preview)
    }

    @Test
    fun `detail html renders badges author labels and escaped body`() {
        val html = PatchTalkThreadPresentation.detailHtml(
            thread(
                path = "src/App.kt",
                line = 18,
                status = ThreadStatus.RESOLVED,
                messageBodies = listOf("Use <strong>less</strong> chrome.", "Agent fixed it."),
                authorTypes = listOf(CommentAuthorType.USER, CommentAuthorType.AGENT),
            ),
        )

        assertContains(html, "Resolved")
        assertContains(html, "App.kt")
        assertContains(html, "Line 18")
        assertContains(html, "You")
        assertContains(html, "Agent")
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
