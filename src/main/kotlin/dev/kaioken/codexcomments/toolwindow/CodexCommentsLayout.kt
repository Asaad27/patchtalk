package dev.kaioken.codexcomments.toolwindow

data class CodexCommentsSplitLayout(
    val leftPanelWidth: Int,
    val resizeWeight: Double,
    val dividerSize: Int,
)

fun codexCommentsSplitLayout(): CodexCommentsSplitLayout {
    return CodexCommentsSplitLayout(
        leftPanelWidth = 232,
        resizeWeight = 0.28,
        dividerSize = 2,
    )
}
