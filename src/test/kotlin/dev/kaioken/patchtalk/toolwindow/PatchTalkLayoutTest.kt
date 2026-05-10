package dev.kaioken.patchtalk.toolwindow

import kotlin.test.Test
import kotlin.test.assertEquals

class PatchTalkLayoutTest {

    @Test
    fun `split layout keeps thread list compact`() {
        val layout = patchTalkSplitLayout()

        assertEquals(180, layout.leftPanelWidth)
        assertEquals(0.2, layout.resizeWeight)
        assertEquals(2, layout.dividerSize)
    }
}
