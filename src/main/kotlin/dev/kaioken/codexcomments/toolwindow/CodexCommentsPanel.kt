package dev.kaioken.codexcomments.toolwindow

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import dev.kaioken.codexcomments.comments.CodexCommentListener
import dev.kaioken.codexcomments.comments.CodexCommentService
import dev.kaioken.codexcomments.comments.CommentAuthorType
import dev.kaioken.codexcomments.comments.CommentThread
import dev.kaioken.codexcomments.comments.ThreadStatus
import dev.kaioken.codexcomments.comments.resolveProjectFile
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import javax.swing.DefaultListModel
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JEditorPane
import javax.swing.JPanel
import javax.swing.JSplitPane
import javax.swing.ListSelectionModel
import javax.swing.SwingConstants
import javax.swing.event.MouseInputAdapter
import javax.swing.text.DefaultCaret

class CodexCommentsPanel(
    private val project: Project,
) : JPanel(BorderLayout()), Disposable, CodexCommentListener {
    private val splitLayout = codexCommentsSplitLayout()
    private val commentService = project.service<CodexCommentService>()
    private val uiService = project.service<CodexCommentsUiService>()

    private val threadModel = DefaultListModel<CommentThread>()
    private val threadList = JBList(threadModel)
    private val statusFilter = JComboBox(arrayOf("Open", "Resolved", "All"))
    private val detailsPane = JEditorPane()
    private val replyArea = JBTextArea(4, 20)
    private val replyButton = JButton("Reply")
    private val resolveButton = JButton("Resolve")
    private val threadCountLabel = JBLabel("0 threads")
    private val emptyStateLabel = JBLabel("Select a comment to inspect the thread", SwingConstants.CENTER)

    init {
        commentService.addListener(this)
        uiService.registerPanel(this)
        buildUi()
        refreshThreads()
    }

    override fun commentsChanged() {
        ApplicationManager.getApplication().invokeLater {
            refreshThreads(selectedThreadId())
        }
    }

    override fun dispose() {
        commentService.removeListener(this)
        uiService.unregisterPanel(this)
    }

    fun selectThread(threadId: String) {
        refreshThreads(threadId)
    }

    private fun buildUi() {
        minimumSize = Dimension(300, 200)
        background = JBColor.PanelBackground

        threadList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        threadList.visibleRowCount = -1
        threadList.fixedCellHeight = -1
        threadList.background = JBColor.PanelBackground
        threadList.border = JBUI.Borders.empty()
        threadList.cellRenderer = ThreadCardRenderer()
        threadList.addListSelectionListener { renderSelectedThread() }
        threadList.addMouseListener(object : MouseInputAdapter() {
            override fun mouseClicked(event: java.awt.event.MouseEvent) {
                if (event.button != java.awt.event.MouseEvent.BUTTON1) return
                val index = threadList.locationToIndex(event.point)
                if (index < 0) return
                if (threadList.getCellBounds(index, index)?.contains(event.point) != true) return
                threadList.selectedIndex = index
                openSelectedThread()
            }
        })

        statusFilter.addActionListener { refreshThreads(selectedThreadId()) }

        detailsPane.isEditable = false
        detailsPane.contentType = "text/html"
        detailsPane.border = JBUI.Borders.empty(8)
        detailsPane.background = JBColor.PanelBackground
        (detailsPane.caret as? DefaultCaret)?.updatePolicy = DefaultCaret.NEVER_UPDATE

        replyArea.lineWrap = true
        replyArea.wrapStyleWord = true
        replyArea.border = BorderFactory.createCompoundBorder(
            JBUI.Borders.customLine(JBColor(0xD8E4F4, 0x3B4B60)),
            JBUI.Borders.empty(8),
        )
        replyArea.font = replyArea.font.deriveFont(Font.PLAIN, replyArea.font.size2D + 1f)

        replyButton.addActionListener { replyToSelectedThread() }
        resolveButton.addActionListener { toggleResolvedState() }
        val leftHeader = JPanel(BorderLayout(8, 0)).apply {
            isOpaque = false
            border = JBUI.Borders.empty(12)
            add(
                JPanel().apply {
                    isOpaque = false
                    layout = BoxLayout(this, BoxLayout.Y_AXIS)
                    add(JBLabel("Comment Threads").apply {
                        font = font.deriveFont(Font.BOLD, font.size2D + 2f)
                    })
                    add(JBLabel("Click a thread to jump to source").apply {
                        foreground = JBColor(0x5F6368, 0x9AA0A6)
                    })
                },
                BorderLayout.CENTER,
            )
            add(
                JPanel(FlowLayout(FlowLayout.RIGHT, 8, 0)).apply {
                    isOpaque = false
                    add(threadCountLabel)
                    add(statusFilter)
                },
                BorderLayout.EAST,
            )
        }

        val leftPanel = JPanel(BorderLayout()).apply {
            background = JBColor.PanelBackground
            add(leftHeader, BorderLayout.NORTH)
            add(JBScrollPane(threadList).apply {
                border = JBUI.Borders.empty()
            }, BorderLayout.CENTER)
            preferredSize = Dimension(splitLayout.leftPanelWidth, 400)
        }

        val rightHeader = JPanel(BorderLayout()).apply {
            isOpaque = false
            border = JBUI.Borders.empty(12, 12, 0, 12)
            add(JBLabel("Thread Detail").apply {
                font = font.deriveFont(Font.BOLD, font.size2D + 2f)
            }, BorderLayout.WEST)
        }

        val rightPanel = JPanel(BorderLayout()).apply {
            background = JBColor.PanelBackground
            add(rightHeader, BorderLayout.NORTH)
            add(
                JBScrollPane(detailsPane).apply {
                    border = JBUI.Borders.empty(4, 8, 0, 8)
                    viewport.background = JBColor.PanelBackground
                },
                BorderLayout.CENTER,
            )
            add(
                JPanel(BorderLayout(0, 8)).apply {
                    border = JBUI.Borders.empty(0, 12, 12, 12)
                    isOpaque = false
                    add(JBLabel("Reply").apply {
                        foreground = JBColor(0x5F6368, 0x9AA0A6)
                    }, BorderLayout.NORTH)
                    add(JBScrollPane(replyArea), BorderLayout.CENTER)
                    add(
                        JPanel(FlowLayout(FlowLayout.RIGHT, 8, 0)).apply {
                            isOpaque = false
                            add(resolveButton)
                            add(replyButton)
                        },
                        BorderLayout.SOUTH,
                    )
                },
                BorderLayout.SOUTH,
            )
        }

        add(JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel).apply {
            resizeWeight = splitLayout.resizeWeight
            dividerSize = splitLayout.dividerSize
            border = JBUI.Borders.empty()
        }, BorderLayout.CENTER)
    }

    private fun selectedThreadId(): String? = threadList.selectedValue?.id

    private fun refreshThreads(preferredThreadId: String? = null) {
        val selectedStatus = when (statusFilter.selectedItem as String) {
            "Open" -> ThreadStatus.OPEN
            "Resolved" -> ThreadStatus.RESOLVED
            else -> null
        }

        val threads = commentService.listThreads(selectedStatus, null)
            .sortedByDescending { it.updatedAt }

        threadModel.removeAllElements()
        threads.forEach(threadModel::addElement)
        threadCountLabel.text = when (threads.size) {
            1 -> "1 thread"
            else -> "${threads.size} threads"
        }

        if (threadModel.isEmpty) {
            detailsPane.text = emptyStateHtml("No comments found for this filter.")
            resolveButton.isEnabled = false
            replyButton.isEnabled = false
            return
        }

        val index = threads.indexOfFirst { it.id == preferredThreadId }.takeIf { it >= 0 } ?: 0
        threadList.selectedIndex = index
        threadList.ensureIndexIsVisible(index)
        renderSelectedThread()
    }

    private fun renderSelectedThread() {
        val thread = threadList.selectedValue
        if (thread == null) {
            detailsPane.text = emptyStateHtml(emptyStateLabel.text)
            resolveButton.isEnabled = false
            replyButton.isEnabled = false
            return
        }

        detailsPane.text = CodexCommentThreadPresentation.detailHtml(thread)
        detailsPane.caretPosition = 0
        resolveButton.text = if (thread.status == ThreadStatus.OPEN) "Resolve" else "Reopen"
        resolveButton.isEnabled = true
        replyButton.isEnabled = true
    }

    private fun replyToSelectedThread() {
        val thread = threadList.selectedValue ?: return
        val reply = replyArea.text.trim()
        if (reply.isEmpty()) return
        commentService.addReply(thread.id, reply, CommentAuthorType.USER)
        replyArea.text = ""
    }

    private fun toggleResolvedState() {
        val thread = threadList.selectedValue ?: return
        if (thread.status == ThreadStatus.OPEN) {
            commentService.resolveThread(thread.id)
        } else {
            commentService.reopenThread(thread.id)
        }
    }

    private fun openSelectedThread() {
        val thread = threadList.selectedValue ?: return
        val file = resolveProjectFile(project, thread.anchor.path) ?: return
        val line = (thread.anchor.line ?: 1) - 1
        OpenFileDescriptor(project, file, line, 0).navigate(true)
    }

    private fun emptyStateHtml(message: String): String {
        return """
        <html>
          <body style="font-family: sans-serif; color: #5F6368; padding: 20px;">
            $message
          </body>
        </html>
        """.trimIndent()
    }

    private inner class ThreadCardRenderer : javax.swing.ListCellRenderer<CommentThread> {
        override fun getListCellRendererComponent(
            list: JList<out CommentThread>,
            value: CommentThread,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean,
        ): java.awt.Component {
            val presentation = CodexCommentThreadPresentation.listItem(value)
            val background = if (isSelected) {
                JBColor(0xEAF3FF, 0x22344A)
            } else {
                JBColor(0xFFFFFF, 0x313335)
            }
            val borderColor = if (isSelected) {
                JBColor(0x8AB4F8, 0x5CA9FF)
            } else {
                JBColor(0xD8E4F4, 0x43474A)
            }

            return JPanel(BorderLayout(0, 8)).apply {
                isOpaque = true
                this.background = background
                border = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(borderColor),
                    JBUI.Borders.empty(10),
                )

                add(
                    JPanel(BorderLayout(8, 0)).apply {
                        isOpaque = false
                        add(
                            JBLabel(presentation.title).apply {
                                font = font.deriveFont(Font.BOLD)
                                foreground = JBColor(0x202124, 0xE8EAED)
                            },
                            BorderLayout.WEST,
                        )
                        add(
                            statusBadge(presentation.statusLabel, value.status),
                            BorderLayout.EAST,
                        )
                    },
                    BorderLayout.NORTH,
                )

                add(
                    JPanel().apply {
                        isOpaque = false
                        layout = BoxLayout(this, BoxLayout.Y_AXIS)
                        add(JBLabel(presentation.location).apply {
                            foreground = JBColor(0x5F6368, 0x9AA0A6)
                        })
                        add(Box.createVerticalStrut(6))
                        add(JBLabel(truncate(presentation.preview, 88)).apply {
                            foreground = JBColor(0x202124, 0xE8EAED)
                        })
                    },
                    BorderLayout.CENTER,
                )

                add(
                    JBLabel(presentation.updatedAtLabel).apply {
                        foreground = JBColor(0x5F6368, 0x9AA0A6)
                    },
                    BorderLayout.SOUTH,
                )
            }
        }

        private fun statusBadge(label: String, status: ThreadStatus): JLabel {
            val background = if (status == ThreadStatus.OPEN) {
                JBColor(0xE8F2FF, 0x1E3A5F)
            } else {
                JBColor(0xEAF7EA, 0x1E4632)
            }
            val foreground = if (status == ThreadStatus.OPEN) {
                JBColor(0x1C5FB8, 0xB9D8FF)
            } else {
                JBColor(0x2B7A3D, 0xA8E7B1)
            }
            return JLabel(label).apply {
                isOpaque = true
                this.background = background
                this.foreground = foreground
                border = JBUI.Borders.empty(4, 8)
                font = font.deriveFont(Font.BOLD, font.size2D - 1f)
            }
        }

        private fun truncate(value: String, maxLength: Int): String {
            return if (value.length <= maxLength) value else value.take(maxLength - 1) + "…"
        }
    }
}
