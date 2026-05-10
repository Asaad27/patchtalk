package dev.kaioken.patchtalk.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import dev.kaioken.patchtalk.toolwindow.PatchTalkUiService

class ShowPatchTalkToolWindowAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        project.service<PatchTalkUiService>().showToolWindow()
    }

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible = event.project != null
    }
}
