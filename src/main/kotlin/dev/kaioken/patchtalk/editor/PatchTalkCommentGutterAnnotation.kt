package dev.kaioken.patchtalk.editor

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorGutterAction
import com.intellij.openapi.editor.TextAnnotationGutterProvider
import com.intellij.openapi.editor.colors.ColorKey
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.ui.JBColor
import dev.kaioken.patchtalk.comments.CommentThread
import java.awt.Color
import java.awt.Cursor

class PatchTalkCommentGutterAnnotation(
    private val threadsByLine: Map<Int, List<CommentThread>>,
    private val onClick: (CommentThread) -> Unit,
) : TextAnnotationGutterProvider, EditorGutterAction {

    override fun getLineText(line: Int, editor: Editor): String? {
        return if (threadsAt(line).isEmpty()) null else MARKER_TEXT
    }

    override fun getToolTip(line: Int, editor: Editor): String? {
        val threads = threadsAt(line)
        if (threads.isEmpty()) return null
        val count = threads.size
        val preview = threads.firstOrNull()?.messages?.lastOrNull()?.body.orEmpty()
        return if (count == 1) {
            "PatchTalk comment: $preview"
        } else {
            "$count PatchTalk comments on this line"
        }
    }

    override fun getStyle(line: Int, editor: Editor): EditorFontType = EditorFontType.PLAIN

    override fun getColor(line: Int, editor: Editor): ColorKey = MARKER_COLOR_KEY

    override fun getBgColor(line: Int, editor: Editor): Color? = null

    override fun getPopupActions(line: Int, editor: Editor): List<AnAction> = emptyList()

    override fun gutterClosed() = Unit

    override fun useMargin(): Boolean = false

    override fun doAction(lineNum: Int) {
        threadsAt(lineNum).firstOrNull()?.let(onClick)
    }

    override fun getCursor(lineNum: Int): Cursor {
        return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
    }

    override fun equals(other: Any?): Boolean {
        return other is PatchTalkCommentGutterAnnotation && threadsByLine.keys == other.threadsByLine.keys
    }

    override fun hashCode(): Int = threadsByLine.keys.hashCode()

    private fun threadsAt(line: Int): List<CommentThread> = threadsByLine[line + 1].orEmpty()

    companion object {
        private const val MARKER_TEXT = "\u25CF"
        private val MARKER_COLOR_KEY = ColorKey.createColorKey(
            "PatchTalk.CommentMarker",
            JBColor(Color(0xD06B00), Color(0xF0B84A)),
        )
    }
}
