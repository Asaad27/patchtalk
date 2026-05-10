package dev.kaioken.codexcomments.toolwindow

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import dev.kaioken.codexcomments.PluginIds

@Service(Service.Level.PROJECT)
class CodexCommentsUiService(
    private val project: Project,
) {
    private var panel: CodexCommentsPanel? = null
    private var pendingSelection: String? = null

    fun registerPanel(panel: CodexCommentsPanel) {
        this.panel = panel
        pendingSelection?.let {
            panel.selectThread(it)
            pendingSelection = null
        }
    }

    fun unregisterPanel(panel: CodexCommentsPanel) {
        if (this.panel === panel) {
            this.panel = null
        }
    }

    fun showToolWindow() {
        ToolWindowManager.getInstance(project).getToolWindow(PluginIds.TOOL_WINDOW_ID)?.show()
    }

    fun showAndSelect(threadId: String) {
        pendingSelection = threadId
        showToolWindow()
        panel?.selectThread(threadId)
        pendingSelection = null
    }
}
