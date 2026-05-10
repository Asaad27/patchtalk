package dev.kaioken.codexcomments.comments

data class CodexCommentProjectState(
    var threads: MutableList<CommentThread> = mutableListOf(),
)

internal fun CodexCommentProjectState.deepCopy(): CodexCommentProjectState = CodexCommentProjectState(
    threads = threads.map { it.deepCopy() }.toMutableList(),
)
