package dev.kaioken.patchtalk.toolwindow

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import dev.kaioken.patchtalk.PluginIds

@Service(Service.Level.PROJECT)
class PatchTalkUiService(
    private val project: Project,
) {
    private var panel: PatchTalkPanel? = null
    private var pendingSelection: String? = null

    fun registerPanel(panel: PatchTalkPanel) {
        this.panel = panel
        pendingSelection?.let {
            panel.selectThread(it)
            pendingSelection = null
        }
    }

    fun unregisterPanel(panel: PatchTalkPanel) {
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
