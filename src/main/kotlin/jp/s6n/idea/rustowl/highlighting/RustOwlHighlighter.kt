package jp.s6n.idea.rustowl.highlighting

import com.intellij.openapi.application.EDT
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.markup.*
import com.intellij.platform.lsp.api.LspServer
import com.intellij.platform.lsp.api.LspServerManager
import com.jetbrains.Service
import jp.s6n.idea.rustowl.configuration.AppSettingsState
import jp.s6n.idea.rustowl.configuration.DecorationType
import jp.s6n.idea.rustowl.lsp.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CompletableFuture

@Service
@Suppress("UnstableApiUsage")
class RustOwlHighlighter(
    private val scope: CoroutineScope
) : IRustOwlHighlighter {
    private val logger = Logger.getInstance(this.javaClass)
    private val decorationsColorMap = AppSettingsState.getInstance().colors

    private var lastHighlighters: MutableList<RangeHighlighter> = mutableListOf()

    override fun highlight(editor: Editor, position: LogicalPosition) {
        logger.debug("Highlighting lifetimes at $position")

        lastHighlighters.forEach {
            editor.markupModel.removeHighlighter(it)
        }

        val project = editor.project ?: return
        val server = LspServerManager
            .getInstance(project)
            .getServersForProvider(RustOwlLspServerSupportProvider::class.java)
            .firstOrNull() ?: return

        scope.launch(Dispatchers.EDT) {
            val fileUrl = editor.virtualFile?.url ?: return@launch
            val response = withContext(Dispatchers.Default) {
                server.getHighlightForCursor(position, fileUrl)
            } ?: return@launch

            logger.debug("RustOwl response: $response")

            editor.applyHighlight(response)
        }
    }

    private suspend fun LspServer.getHighlightForCursor(
        position: LogicalPosition,
        fileUrl: String
    ): RustOwlCursorResponse? {
        return sendRequest {
            if (it !is RustOwlLsp4jServer) {
                return@sendRequest CompletableFuture.failedFuture(Throwable("We have the wrong server????"))
            }

            it.cursor(
                RustOwlCursorRequest(
                    Position(position.line, position.column),
                    Document(fileUrl),
                )
            )
        }
    }

    private fun Editor.applyHighlight(response: RustOwlCursorResponse) {
        for (decoration in response.decorations) {
            val decorationType = DecorationType.fromString(decoration.type) ?: continue
            val color = decorationsColorMap[decorationType] ?: decorationType.defaultColor
            val range = decoration.range
            val textAttributes = TextAttributes().apply {
                withAdditionalEffect(EffectType.BOLD_LINE_UNDERSCORE, color)
            }

            val highlighter = markupModel.addRangeHighlighter(
                range.start.toOffset(this),
                range.end.toOffset(this),
                HighlighterLayer.LAST,
                textAttributes,
                HighlighterTargetArea.EXACT_RANGE
            )

            lastHighlighters.add(highlighter)
        }
    }
}
