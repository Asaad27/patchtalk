package dev.kaioken.patchtalk.comments

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CopyOnWriteArrayList

// Keep the original state key and storage filename so existing local threads survive the public rebrand.
@State(
    name = "CodexCommentProjectState",
    storages = [Storage("codex-comments.xml")],
)
class PatchTalkCommentService : PersistentStateComponent<PatchTalkCommentProjectState> {
    private var state = PatchTalkCommentProjectState()
    private val listeners = CopyOnWriteArrayList<PatchTalkCommentListener>()

    override fun getState(): PatchTalkCommentProjectState = state.deepCopy()

    override fun loadState(state: PatchTalkCommentProjectState) {
        this.state = state.deepCopy()
        notifyChanged()
    }

    fun addListener(listener: PatchTalkCommentListener) {
        listeners += listener
    }

    fun removeListener(listener: PatchTalkCommentListener) {
        listeners -= listener
    }

    fun createFileThread(path: String, body: String, author: CommentAuthorType): CommentThread {
        return createThread(path = path, line = null, body = body, author = author)
    }

    fun createLineThread(path: String, line: Int, body: String, author: CommentAuthorType): CommentThread {
        require(line > 0) { "line must be greater than zero" }
        return createThread(path = path, line = line, body = body, author = author)
    }

    fun addReply(threadId: String, body: String, author: CommentAuthorType): CommentThread {
        val normalizedBody = normalizeBody(body)
        val thread = requireThread(threadId)
        val updated = thread.copy(
            messages = (thread.messages + newMessage(normalizedBody, author)).toMutableList(),
            updatedAt = now(),
        )
        replaceThread(updated)
        return updated.deepCopy()
    }

    fun updateThreadLine(threadId: String, line: Int): CommentThread {
        require(line > 0) { "line must be greater than zero" }
        val thread = requireThread(threadId)
        val updated = thread.copy(
            anchor = thread.anchor.copy(line = line),
            updatedAt = now(),
        )
        replaceThread(updated)
        return updated.deepCopy()
    }

    fun resolveThread(threadId: String): CommentThread {
        return updateThreadStatus(threadId, ThreadStatus.RESOLVED)
    }

    fun reopenThread(threadId: String): CommentThread {
        return updateThreadStatus(threadId, ThreadStatus.OPEN)
    }

    fun getThread(threadId: String): CommentThread? {
        return state.threads.firstOrNull { it.id == threadId }?.deepCopy()
    }

    fun listThreads(status: ThreadStatus? = null, path: String? = null): List<CommentThread> {
        return state.threads
            .asSequence()
            .filter { status == null || it.status == status }
            .filter { path == null || it.anchor.path == path.trim() }
            .map { it.deepCopy() }
            .toList()
    }

    private fun createThread(path: String, line: Int?, body: String, author: CommentAuthorType): CommentThread {
        val normalizedPath = normalizePath(path)
        val normalizedBody = normalizeBody(body)
        val timestamp = now()
        val thread = CommentThread(
            id = UUID.randomUUID().toString(),
            anchor = CommentAnchor(path = normalizedPath, line = line),
            status = ThreadStatus.OPEN,
            messages = mutableListOf(newMessage(normalizedBody, author, timestamp)),
            createdAt = timestamp,
            updatedAt = timestamp,
        )
        state.threads += thread
        notifyChanged()
        return thread.deepCopy()
    }

    private fun updateThreadStatus(threadId: String, status: ThreadStatus): CommentThread {
        val thread = requireThread(threadId)
        val updated = thread.copy(
            status = status,
            updatedAt = now(),
        )
        replaceThread(updated)
        return updated.deepCopy()
    }

    private fun replaceThread(updated: CommentThread) {
        val index = state.threads.indexOfFirst { it.id == updated.id }
        check(index >= 0) { "Thread not found: ${updated.id}" }
        state.threads[index] = updated
        notifyChanged()
    }

    private fun requireThread(threadId: String): CommentThread {
        return state.threads.firstOrNull { it.id == threadId }
            ?: throw IllegalArgumentException("Thread not found: $threadId")
    }

    private fun newMessage(body: String, author: CommentAuthorType, timestamp: String = now()): CommentMessage {
        return CommentMessage(
            id = UUID.randomUUID().toString(),
            authorType = author,
            body = body,
            createdAt = timestamp,
        )
    }

    private fun normalizePath(path: String): String {
        val normalized = path.trim().replace('\\', '/')
        require(normalized.isNotBlank()) { "path must not be blank" }
        return normalized
    }

    private fun normalizeBody(body: String): String {
        val normalized = body.trim()
        require(normalized.isNotBlank()) { "body must not be blank" }
        return normalized
    }

    private fun notifyChanged() {
        listeners.forEach { it.commentsChanged() }
    }

    private fun now(): String = Instant.now().toString()
}
