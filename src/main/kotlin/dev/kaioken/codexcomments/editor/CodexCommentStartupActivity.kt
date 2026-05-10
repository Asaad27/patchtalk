package dev.kaioken.codexcomments.editor

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

class CodexCommentStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        // Force initialization of the presentation service so it can attach its EditorFactoryListener
        project.service<CodexCommentEditorPresentationService>()
    }
}
