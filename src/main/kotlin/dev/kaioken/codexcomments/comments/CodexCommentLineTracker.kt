package dev.kaioken.codexcomments.comments

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener

class CodexCommentLineTracker(
    private val commentService: CodexCommentService,
) {
    private data class TrackedDocument(
        val document: Document,
        val listener: DocumentListener,
        val markersByThreadId: Map<String, RangeMarker>,
    )

    private val trackedByPath = linkedMapOf<String, TrackedDocument>()

    fun track(path: String, document: Document) {
        stopTracking(path)

        val markersByThreadId = commentService
            .listThreads(path = path)
            .filter { it.anchor.line != null }
            .associate { thread ->
                thread.id to createMarker(document, thread.anchor.line!!)
            }

        val listener = object : DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                syncTrackedLines(path)
            }
        }

        document.addDocumentListener(listener)
        trackedByPath[path] = TrackedDocument(
            document = document,
            listener = listener,
            markersByThreadId = markersByThreadId,
        )
        syncTrackedLines(path)
    }

    fun stopTracking(path: String) {
        val tracked = trackedByPath.remove(path) ?: return
        tracked.document.removeDocumentListener(tracked.listener)
        tracked.markersByThreadId.values.forEach { it.dispose() }
    }

    private fun syncTrackedLines(path: String) {
        val tracked = trackedByPath[path] ?: return
        tracked.markersByThreadId.forEach { (threadId, marker) ->
            if (!marker.isValid) return@forEach
            val newLine = tracked.document.getLineNumber(marker.startOffset) + 1
            val currentLine = commentService.getThread(threadId)?.anchor?.line
            if (currentLine != newLine) {
                commentService.updateThreadLine(threadId, newLine)
            }
        }
    }

    private fun createMarker(document: Document, line: Int): RangeMarker {
        val zeroBasedLine = (line - 1).coerceIn(0, document.lineCount - 1)
        val offset = document.getLineStartOffset(zeroBasedLine)
        return document.createRangeMarker(offset, offset)
    }
}
