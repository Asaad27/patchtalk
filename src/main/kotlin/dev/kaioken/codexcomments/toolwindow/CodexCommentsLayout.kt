package dev.kaioken.codexcomments.toolwindow

data class CodexCommentsSplitLayout(
    val leftPanelWidth: Int,
    val resizeWeight: Double,
    val dividerSize: Int,
)

fun codexCommentsSplitLayout(): CodexCommentsSplitLayout {
    return CodexCommentsSplitLayout(
        leftPanelWidth = 180,
        resizeWeight = 0.2,
        dividerSize = 2,
    )
}
