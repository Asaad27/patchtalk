package dev.kaioken.patchtalk.comments

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.Path
import kotlin.io.path.invariantSeparatorsPathString

fun relativeProjectPath(project: Project, virtualFile: VirtualFile): String? {
    val basePath = project.basePath ?: return null
    val projectPath = Path.of(basePath)
    val filePath = Path.of(virtualFile.path)
    if (!filePath.startsWith(projectPath)) return null
    return projectPath.relativize(filePath).invariantSeparatorsPathString
}

fun resolveProjectFile(project: Project, relativePath: String): VirtualFile? {
    val basePath = project.basePath ?: return null
    val absolutePath = Path.of(basePath).resolve(relativePath).normalize().toString()
    return LocalFileSystem.getInstance().findFileByPath(absolutePath)
}
