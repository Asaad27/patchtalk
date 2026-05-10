package dev.kaioken.codexcomments.toolwindow

import kotlin.test.Test
import kotlin.test.assertEquals

class CodexCommentsLayoutTest {

    @Test
    fun `split layout keeps thread list compact`() {
        val layout = codexCommentsSplitLayout()

        assertEquals(180, layout.leftPanelWidth)
        assertEquals(0.2, layout.resizeWeight)
        assertEquals(2, layout.dividerSize)
    }
}
