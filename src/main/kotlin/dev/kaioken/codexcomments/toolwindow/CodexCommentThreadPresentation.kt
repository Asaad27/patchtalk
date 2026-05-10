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
            val isUser = message.authorType == CommentAuthorType.USER
            val accent = if (!isUser) {
                htmlColor(JBColor(0xF9F5FF, 0x1B1324))
            } else {
                htmlColor(JBColor(0xF8F9FA, 0x1F2126))
            }
            val border = if (!isUser) {
                htmlColor(JBColor(0xDCCEFF, 0x3B255E))
            } else {
                htmlColor(JBColor(0xE8EAED, 0x2A2D35))
            }
            val authorColor = if (!isUser) {
                htmlColor(JBColor(0x603A96, 0xBB86FC))
            } else {
                htmlColor(JBColor(0x1A73E8, 0x8AB4F8))
            }
            
            val align = if (isUser) "right" else "left"
            val marginLeft = if (isUser) "40px" else "0"
            val marginRight = if (isUser) "0" else "40px"
            
            """
            <div style="margin-top: 0px; margin-bottom: 16px; margin-left: $marginLeft; margin-right: $marginRight;">
              <table width="100%" cellpadding="12" cellspacing="0" style="border: 1px solid $border; background-color: $accent;">
                <tr>
                  <td>
                    <div style="text-align: $align; margin-bottom: 8px;">
                      <b style="color:$authorColor; font-size:12px;">${escape(authorLabel)}</b>
                      <span style="color:$secondaryText; font-size:10px;">&nbsp;&nbsp;${escape(formatTimestamp(message.createdAt))}</span>
                    </div>
                    <div style="color:$primaryText; font-size:13px; line-height: 1.5; text-align: left;">
                      ${escape(message.body).replace("\n", "<br/>")}
                    </div>
                  </td>
                </tr>
              </table>
            </div>
            """.trimIndent()
        }

        return """
        <html>
          <body style="font-family: -apple-system, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; margin: 0; padding: 16px; background-color: $bodyBackground;">
            <div style="margin-bottom: 24px; border-bottom: 1px solid ${htmlColor(JBColor(0xE8EAED, 0x313335))};">
              <table width="100%" cellpadding="0" cellspacing="0">
                <tr>
                  <td style="padding-bottom: 6px;">
                    <b style="font-size: 18px; color: $primaryText;">$title</b>
                  </td>
                </tr>
                <tr>
                  <td style="padding-bottom: 16px;">
                    <table cellpadding="4" cellspacing="0">
                      <tr>
                        <td style="background-color: $statusBackground; border: 1px solid $statusBackground;">
                          <b style="color: $statusForeground; font-size: 10px;">${escape(statusLabel)}</b>
                        </td>
                        <td>
                          <span style="color: $secondaryText; font-size: 11px;">&nbsp;&nbsp;$location</span>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
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
