package dev.kaioken.codexcomments.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import dev.kaioken.codexcomments.comments.CodexCommentService
import dev.kaioken.codexcomments.comments.CommentAuthorType
import dev.kaioken.codexcomments.toolwindow.CodexCommentsUiService

class AddFileCommentAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val context = resolveCommentActionContext(event) ?: return
        val body = promptForComment(context.project, "Add Codex File Comment") ?: return
        val thread = context.project.service<CodexCommentService>().createFileThread(
            path = context.relativePath,
            body = body,
            author = CommentAuthorType.USER,
        )
        context.project.service<CodexCommentsUiService>().showAndSelect(thread.id)
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible = resolveCommentActionContext(event) != null
    }
}
