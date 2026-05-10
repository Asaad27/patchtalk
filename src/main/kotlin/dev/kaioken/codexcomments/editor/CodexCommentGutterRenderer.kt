package dev.kaioken.codexcomments.editor

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.markup.GutterIconRenderer
import dev.kaioken.codexcomments.comments.CommentThread

class CodexCommentGutterRenderer(
    private val threads: List<CommentThread>,
    private val onClick: () -> Unit,
) : GutterIconRenderer() {

    override fun getIcon() = CodexCommentIcons.COMMENT

    override fun getAlignment(): Alignment = Alignment.LINE_NUMBERS

    override fun getTooltipText(): String {
        val count = threads.size
        val preview = threads.firstOrNull()?.messages?.lastOrNull()?.body.orEmpty()
        return if (count == 1) {
            "Codex comment: $preview"
        } else {
            "$count Codex comments on this line"
        }
    }

    override fun getClickAction(): AnAction {
        return object : AnAction() {
            override fun actionPerformed(e: AnActionEvent) {
                onClick()
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is CodexCommentGutterRenderer && threads.map { it.id } == other.threads.map { it.id }
    }

    override fun hashCode(): Int = threads.map { it.id }.hashCode()
}
