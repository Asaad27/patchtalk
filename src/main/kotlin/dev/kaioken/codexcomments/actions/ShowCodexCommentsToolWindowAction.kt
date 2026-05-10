package dev.kaioken.codexcomments.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import dev.kaioken.codexcomments.toolwindow.CodexCommentsUiService

class ShowCodexCommentsToolWindowAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        project.service<CodexCommentsUiService>().showToolWindow()
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible = event.project != null
    }
}
