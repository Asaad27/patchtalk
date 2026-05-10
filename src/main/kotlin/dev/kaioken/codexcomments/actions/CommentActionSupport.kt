package dev.kaioken.codexcomments.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import dev.kaioken.codexcomments.comments.relativeProjectPath

internal data class CommentActionContext(
    val project: Project,
    val virtualFile: VirtualFile,
    val relativePath: String,
)

internal fun resolveCommentActionContext(event: AnActionEvent): CommentActionContext? {
    val project = event.project ?: return null
    val virtualFile = event.getData(CommonDataKeys.VIRTUAL_FILE)
        ?: event.getData(CommonDataKeys.EDITOR)
            ?.let { FileDocumentManager.getInstance().getFile(it.document) }
        ?: return null
    val relativePath = relativeProjectPath(project, virtualFile) ?: return null
    return CommentActionContext(project, virtualFile, relativePath)
}

internal fun promptForComment(project: Project, title: String): String? {
    val body = Messages.showMultilineInputDialog(
        project,
        "Enter a comment for Codex.",
        title,
        "",
        null,
        null,
    ) ?: return null
    return body.takeIf { it.trim().isNotEmpty() }
}
