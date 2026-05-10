package dev.kaioken.codexcomments.comments

enum class CommentAuthorType {
    USER,
    CODEX,
}

enum class ThreadStatus {
    OPEN,
    RESOLVED,
}

data class CommentAnchor(
    var path: String = "",
    var line: Int? = null,
)

data class CommentMessage(
    var id: String = "",
    var authorType: CommentAuthorType = CommentAuthorType.USER,
    var body: String = "",
    var createdAt: String = "",
)

data class CommentThread(
    var id: String = "",
    var anchor: CommentAnchor = CommentAnchor(),
    var status: ThreadStatus = ThreadStatus.OPEN,
    var messages: MutableList<CommentMessage> = mutableListOf(),
    var createdAt: String = "",
    var updatedAt: String = "",
)

internal fun CommentThread.deepCopy(): CommentThread = copy(
    anchor = anchor.copy(),
    messages = messages.map { it.copy() }.toMutableList(),
)
