package jp.s6n.idea.rustowl.lsp

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.lsp.api.ProjectWideLspServerDescriptor
import jp.s6n.idea.rustowl.configuration.AppSettingsState

private const val FILE_EXTENSION = "rs"

@Suppress("UnstableApiUsage")
class RustOwlLspServerDescriptor(
    project: Project
) : ProjectWideLspServerDescriptor(project, "RustOwl") {
    override val lsp4jServerClass = RustOwlLsp4jServer::class.java

    override fun isSupportedFile(file: VirtualFile) = file.extension == FILE_EXTENSION

    override fun getLanguageId(file: VirtualFile) = "rust"

    override fun createCommandLine() = GeneralCommandLine(AppSettingsState.getInstance().cargoOwlspPath)
}
