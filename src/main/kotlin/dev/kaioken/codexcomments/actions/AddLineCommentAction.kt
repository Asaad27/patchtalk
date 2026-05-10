package dev.kaioken.codexcomments.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import dev.kaioken.codexcomments.comments.CodexCommentService
import dev.kaioken.codexcomments.comments.CommentAuthorType
import dev.kaioken.codexcomments.toolwindow.CodexCommentsUiService

class AddLineCommentAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val context = resolveCommentActionContext(event) ?: return
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return
        val body = promptForComment(context.project, "Add Codex Line Comment") ?: return
        val line = editor.caretModel.logicalPosition.line + 1
        val thread = context.project.service<CodexCommentService>().createLineThread(
            path = context.relativePath,
            line = line,
            body = body,
            author = CommentAuthorType.USER,
        )
        context.project.service<CodexCommentsUiService>().showAndSelect(thread.id)
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible =
            resolveCommentActionContext(event) != null && event.getData(CommonDataKeys.EDITOR) != null
    }
}
