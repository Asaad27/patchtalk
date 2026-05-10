package dev.kaioken.codexcomments.toolwindow

import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.ColorUtil
import com.intellij.ui.JBColor
import com.intellij.util.text.DateFormatUtil
import dev.kaioken.codexcomments.comments.CommentAuthorType
import dev.kaioken.codexcomments.comments.CommentThread
import dev.kaioken.codexcomments.comments.ThreadStatus
import java.nio.file.Paths
import java.awt.Color
import java.time.Instant
import java.util.Date

data class ThreadListItemPresentation(
    val title: String,
    val location: String,
    val statusLabel: String,
    val preview: String,
    val updatedAtLabel: String,
)

object CodexCommentThreadPresentation {
    fun listItem(thread: CommentThread): ThreadListItemPresentation {
        return ThreadListItemPresentation(
            title = fileName(thread.anchor.path),
            location = thread.anchor.line?.let { "Line $it" } ?: "File comment",
            statusLabel = thread.status.label(),
            preview = thread.messages.lastOrNull()?.body.orEmpty(),
            updatedAtLabel = formatTimestamp(thread.updatedAt),
        )
    }

    fun detailHtml(thread: CommentThread): String {
        val bodyBackground = htmlColor(JBColor.PanelBackground)
        val primaryText = htmlColor(JBColor(0x202124, 0xE8EAED))
        val secondaryText = htmlColor(JBColor(0x5F6368, 0x9AA0A6))
        val title = escape(fileName(thread.anchor.path))
        val path = escape(thread.anchor.path)
        val location = escape(thread.anchor.line?.let { "Line $it" } ?: "File comment")
        val statusLabel = thread.status.label()
        val statusBackground = if (thread.status == ThreadStatus.OPEN) {
            htmlColor(JBColor(0xE8F2FF, 0x1E3A5F))
        } else {
            htmlColor(JBColor(0xEAF7EA, 0x1E4632))
        }
        val statusForeground = if (thread.status == ThreadStatus.OPEN) {
            htmlColor(JBColor(0x1C5FB8, 0xB9D8FF))
        } else {
            htmlColor(JBColor(0x2B7A3D, 0xA8E7B1))
        }

        val messagesHtml = thread.messages.joinToString(separator = "") { message ->
            val authorLabel = message.authorType.label()
            val accent = if (message.authorType == CommentAuthorType.CODEX) {
                htmlColor(JBColor(0xF5F0FF, 0x3A2F55))
            } else {
                htmlColor(JBColor(0xF5F9FF, 0x25364C))
            }
            val border = if (message.authorType == CommentAuthorType.CODEX) {
                htmlColor(JBColor(0xDCCEFF, 0x705B9A))
            } else {
                htmlColor(JBColor(0xD7E7FF, 0x4D6D92))
            }
            """
            <div style="margin:0 0 12px 0; padding:12px; background:$accent; border:1px solid $border;">
              <div style="font-weight:bold; color:$primaryText; margin-bottom:6px;">${escape(authorLabel)}</div>
              <div style="color:$secondaryText; font-size:11px; margin-bottom:8px;">${escape(formatTimestamp(message.createdAt))}</div>
              <div style="color:$primaryText; line-height:1.5;">${escape(message.body).replace("\n", "<br/>")}</div>
            </div>
            """.trimIndent()
        }

        return """
        <html>
          <body style="font-family: sans-serif; margin: 0; padding: 12px; background: $bodyBackground;">
            <div style="margin-bottom: 14px;">
              <div style="font-size: 18px; font-weight: bold; color: $primaryText;">$title</div>
              <div style="margin-top: 4px; color: $secondaryText;">$location</div>
              <div style="margin-top: 4px; color: $secondaryText;">$path</div>
              <div style="margin-top: 10px;">
                <span style="background: $statusBackground; color: $statusForeground; font-weight: bold; padding: 4px 8px;">
                  ${escape(statusLabel)}
                </span>
              </div>
            </div>
            $messagesHtml
          </body>
        </html>
        """.trimIndent()
    }

    private fun ThreadStatus.label(): String = name.lowercase().replaceFirstChar(Char::uppercase)

    private fun CommentAuthorType.label(): String = when (this) {
        CommentAuthorType.USER -> "You"
        CommentAuthorType.CODEX -> "Codex"
    }

    private fun fileName(path: String): String {
        return runCatching {
            Paths.get(path.replace('/', '\\')).fileName?.toString()
        }.getOrNull().takeUnless(StringUtil::isEmptyOrSpaces) ?: path
    }

    private fun formatTimestamp(value: String): String {
        return runCatching {
            DateFormatUtil.formatPrettyDateTime(Date.from(Instant.parse(value)))
        }.getOrDefault(value)
    }

    private fun escape(value: String): String = StringUtil.escapeXmlEntities(value)

    private fun htmlColor(color: Color): String = "#${ColorUtil.toHex(color)}"
}
