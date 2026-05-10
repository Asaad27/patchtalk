package dev.kaioken.patchtalk.comments

data class PatchTalkCommentProjectState(
    var threads: MutableList<CommentThread> = mutableListOf(),
)

internal fun PatchTalkCommentProjectState.deepCopy(): PatchTalkCommentProjectState = PatchTalkCommentProjectState(
    threads = threads.map { it.deepCopy() }.toMutableList(),
)
