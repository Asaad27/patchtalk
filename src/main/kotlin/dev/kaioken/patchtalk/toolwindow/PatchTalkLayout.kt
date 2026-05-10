package dev.kaioken.patchtalk.toolwindow

data class PatchTalkSplitLayout(
    val leftPanelWidth: Int,
    val resizeWeight: Double,
    val dividerSize: Int,
)

fun patchTalkSplitLayout(): PatchTalkSplitLayout {
    return PatchTalkSplitLayout(
        leftPanelWidth = 180,
        resizeWeight = 0.2,
        dividerSize = 2,
    )
}
