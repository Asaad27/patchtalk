package dev.kaioken.patchtalk.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import dev.kaioken.patchtalk.comments.PatchTalkCommentService
import dev.kaioken.patchtalk.comments.CommentAuthorType
import dev.kaioken.patchtalk.toolwindow.PatchTalkUiService

class AddFileCommentAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val context = resolveCommentActionContext(event) ?: return
        val body = promptForComment(context.project, "Add PatchTalk File Comment") ?: return
        val thread = context.project.service<PatchTalkCommentService>().createFileThread(
            path = context.relativePath,
            body = body,
            author = CommentAuthorType.USER,
        )
        context.project.service<PatchTalkUiService>().showAndSelect(thread.id)
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible = resolveCommentActionContext(event) != null
    }
}
