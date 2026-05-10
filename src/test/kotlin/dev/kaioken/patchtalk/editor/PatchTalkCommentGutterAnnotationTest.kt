package dev.kaioken.patchtalk.editor

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorFontType
import dev.kaioken.patchtalk.comments.CommentAnchor
import dev.kaioken.patchtalk.comments.CommentAuthorType
import dev.kaioken.patchtalk.comments.CommentMessage
import dev.kaioken.patchtalk.comments.CommentThread
import dev.kaioken.patchtalk.comments.ThreadStatus
import java.awt.Cursor
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PatchTalkCommentGutterAnnotationTest {

    @Test
    fun `annotation exposes supported gutter interfaces`() {
        val annotation = PatchTalkCommentGutterAnnotation(mapOf(12 to listOf(thread("Refactor this branch.")))) {}

        assertEquals("\u25CF", annotation.getLineText(11, editor = fakeEditor()))
        assertEquals(EditorFontType.PLAIN, annotation.getStyle(11, editor = fakeEditor()))
        assertEquals(Cursor.HAND_CURSOR, annotation.getCursor(11).type)
        assertTrue(annotation.getPopupActions(11, editor = fakeEditor()).isEmpty())
    }

    @Test
    fun `annotation tooltip shows latest preview`() {
        val annotation = PatchTalkCommentGutterAnnotation(mapOf(12 to listOf(thread("Original note.", "Latest note.")))) {}

        assertTrue(annotation.getToolTip(11, editor = fakeEditor()).orEmpty().contains("Latest note."))
    }

    @Test
    fun `annotation is absent on unrelated lines`() {
        val annotation = PatchTalkCommentGutterAnnotation(mapOf(12 to listOf(thread("Original note.")))) {}

        assertNull(annotation.getLineText(0, editor = fakeEditor()))
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

    private fun fakeEditor(): Editor {
        return Proxy.newProxyInstance(
            javaClass.classLoader,
            arrayOf(Editor::class.java),
        ) { _, method, _ ->
            when (method.returnType) {
                Boolean::class.javaPrimitiveType -> false
                Int::class.javaPrimitiveType -> 0
                Long::class.javaPrimitiveType -> 0L
                Float::class.javaPrimitiveType -> 0f
                Double::class.javaPrimitiveType -> 0.0
                Char::class.javaPrimitiveType -> '\u0000'
                else -> null
            }
        } as Editor
    }
}
