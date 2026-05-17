package dev.kaioken.patchtalk.editor

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import dev.kaioken.patchtalk.comments.PatchTalkCommentLineTracker
import dev.kaioken.patchtalk.comments.PatchTalkCommentListener
import dev.kaioken.patchtalk.comments.PatchTalkCommentService
import dev.kaioken.patchtalk.comments.relativeProjectPath
import dev.kaioken.patchtalk.toolwindow.PatchTalkUiService

class PatchTalkCommentEditorPresentationService(
    private val project: Project,
) : Disposable, PatchTalkCommentListener {
    private val commentService = project.service<PatchTalkCommentService>()
    private val uiService = project.service<PatchTalkUiService>()
    private val fileDocumentManager = FileDocumentManager.getInstance()
    private val lineTracker = PatchTalkCommentLineTracker(commentService)

    private val pathByEditor = linkedMapOf<Editor, String>()
    private val editorsByPath = linkedMapOf<String, LinkedHashSet<Editor>>()
    private val highlightersByEditor = linkedMapOf<Editor, MutableList<RangeHighlighter>>()

    private val editorListener = object : EditorFactoryListener {
        override fun editorCreated(event: EditorFactoryEvent) {
            bindEditor(event.editor)
        }

        override fun editorReleased(event: EditorFactoryEvent) {
            unbindEditor(event.editor)
        }
    }

    init {
        commentService.addListener(this)
        EditorFactory.getInstance().addEditorFactoryListener(editorListener, this)
        EditorFactory.getInstance().allEditors.forEach(::bindEditor)
    }

    override fun commentsChanged() {
        refreshAllEditors()
    }

    override fun dispose() {
        commentService.removeListener(this)
        pathByEditor.keys.toList().forEach(::unbindEditor)
    }

    private fun bindEditor(editor: Editor) {
        if (editor.project != project || pathByEditor.containsKey(editor)) return
        val path = resolveEditorPath(editor) ?: return
        pathByEditor[editor] = path
        val editorsForPath = editorsByPath.getOrPut(path) { linkedSetOf() }
        editorsForPath += editor
        if (editorsForPath.size == 1) {
            lineTracker.track(path, editor.document)
        }
        refreshEditor(editor, path)
    }

    private fun unbindEditor(editor: Editor) {
        clearHighlighters(editor)
        val path = pathByEditor.remove(editor) ?: return
        val editorsForPath = editorsByPath[path] ?: return
        editorsForPath -= editor
        if (editorsForPath.isEmpty()) {
            editorsByPath.remove(path)
            lineTracker.stopTracking(path)
        }
    }

    private fun refreshAllEditors() {
        pathByEditor.forEach { (editor, path) -> refreshEditor(editor, path) }
    }

    private fun refreshEditor(editor: Editor, path: String) {
        clearHighlighters(editor)

        val threadsByLine = PatchTalkCommentPresentationModel.openThreadsByLine(
            commentService.listThreads(path = path),
        )

        if (threadsByLine.isEmpty()) return

        val document = editor.document
        val highlighters = mutableListOf<RangeHighlighter>()
        threadsByLine.forEach { (line, threads) ->
            val zeroBasedLine = line - 1
            if (zeroBasedLine !in 0 until document.lineCount) return@forEach
            val highlighter = editor.markupModel.addLineHighlighter(zeroBasedLine, HighlighterLayer.FIRST, null)
            highlighter.gutterIconRenderer = PatchTalkCommentGutterRenderer(threads) {
                uiService.showAndSelect(threads.first().id)
            }
            highlighters += highlighter
        }
        highlightersByEditor[editor] = highlighters
    }

    private fun clearHighlighters(editor: Editor) {
        highlightersByEditor.remove(editor)?.forEach { it.dispose() }
    }

    private fun resolveEditorPath(editor: Editor): String? {
        val virtualFile = fileDocumentManager.getFile(editor.document) ?: return null
        return relativeProjectPath(project, virtualFile)
    }
}
