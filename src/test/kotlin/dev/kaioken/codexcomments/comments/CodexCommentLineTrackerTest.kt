package dev.kaioken.codexcomments.comments

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.Document
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import kotlin.test.assertEquals

class CodexCommentLineTrackerTest : BasePlatformTestCase() {

    fun testTrackingUpdatesStoredLineWhenTextIsInsertedBeforeAnchor() {
        val service = CodexCommentService()
        val thread = service.createLineThread(
            path = "src/App.kt",
            line = 2,
            body = "Watch this branch.",
            author = CommentAuthorType.USER,
        )
        val document = EditorFactory.getInstance().createDocument("first\nsecond\nthird\n")
        val tracker = CodexCommentLineTracker(service)

        tracker.track("src/App.kt", document)
        insertText(document, 0, "header\n")

        assertEquals(3, service.getThread(thread.id)?.anchor?.line)
    }

    fun testTrackingIgnoresFileComments() {
        val service = CodexCommentService()
        val thread = service.createFileThread(
            path = "src/App.kt",
            body = "General file note.",
            author = CommentAuthorType.USER,
        )
        val document = EditorFactory.getInstance().createDocument("first\nsecond\nthird\n")
        val tracker = CodexCommentLineTracker(service)

        tracker.track("src/App.kt", document)
        insertText(document, 0, "header\n")

        assertEquals(null, service.getThread(thread.id)?.anchor?.line)
    }

    fun testStopTrackingPreventsFurtherLineUpdates() {
        val service = CodexCommentService()
        val thread = service.createLineThread(
            path = "src/App.kt",
            line = 2,
            body = "Watch this branch.",
            author = CommentAuthorType.USER,
        )
        val document = EditorFactory.getInstance().createDocument("first\nsecond\nthird\n")
        val tracker = CodexCommentLineTracker(service)

        tracker.track("src/App.kt", document)
        tracker.stopTracking("src/App.kt")
        insertText(document, 0, "header\n")

        assertEquals(2, service.getThread(thread.id)?.anchor?.line)
    }

    private fun insertText(document: Document, offset: Int, text: String) {
        ApplicationManager.getApplication().invokeAndWait {
            WriteCommandAction.runWriteCommandAction(project) {
                document.insertString(offset, text)
            }
        }
    }
}
