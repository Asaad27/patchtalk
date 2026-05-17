package dev.kaioken.patchtalk.editor

import dev.kaioken.patchtalk.comments.CommentThread
import dev.kaioken.patchtalk.comments.ThreadStatus

internal object PatchTalkCommentPresentationModel {
    fun openThreadsByLine(threads: List<CommentThread>): Map<Int, List<CommentThread>> {
        return threads.asSequence()
            .filter { it.status == ThreadStatus.OPEN }
            .filter { it.anchor.line != null }
            .groupBy { it.anchor.line!! }
    }
}
