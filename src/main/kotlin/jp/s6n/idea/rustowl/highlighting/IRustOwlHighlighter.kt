package jp.s6n.idea.rustowl.highlighting

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition

interface IRustOwlHighlighter {
    fun highlight(editor: Editor, position: LogicalPosition)
}
