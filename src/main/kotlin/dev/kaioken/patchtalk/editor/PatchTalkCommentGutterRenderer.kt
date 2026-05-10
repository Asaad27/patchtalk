package dev.kaioken.patchtalk.editor

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.markup.GutterIconRenderer
import dev.kaioken.patchtalk.comments.CommentThread

class PatchTalkCommentGutterRenderer(
    private val threads: List<CommentThread>,
    private val onClick: () -> Unit,
) : GutterIconRenderer() {

    override fun getIcon() = PatchTalkCommentIcons.COMMENT

    override fun getAlignment(): Alignment = Alignment.LEFT

    override fun getTooltipText(): String {
        val count = threads.size
        val preview = threads.firstOrNull()?.messages?.lastOrNull()?.body.orEmpty()
        return if (count == 1) {
            "PatchTalk comment: $preview"
        } else {
            "$count PatchTalk comments on this line"
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
        return other is PatchTalkCommentGutterRenderer && threads.map { it.id } == other.threads.map { it.id }
    }

    override fun hashCode(): Int = threads.map { it.id }.hashCode()
}
