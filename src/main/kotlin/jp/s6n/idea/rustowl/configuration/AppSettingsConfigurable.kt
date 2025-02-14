package jp.s6n.idea.rustowl.configuration

import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import javax.swing.JComponent

class AppSettingsConfigurable : Configurable {
    private var settingsComponent: AppSettingsComponent? = null

    @Nls(capitalization = Nls.Capitalization.Title)
    override fun getDisplayName(): String = "RustOwl"

    override fun createComponent(): JComponent {
        settingsComponent = AppSettingsComponent()
        return settingsComponent!!.getPanel()
    }

    override fun isModified(): Boolean {
        val state = AppSettingsState.getInstance()
        return state.cargoOwlspPath != settingsComponent?.cargoOwlspPathField?.text ||
            state.colors != settingsComponent?.getColors()
    }

    override fun apply() {
        val state = AppSettingsState.getInstance()
        state.cargoOwlspPath = settingsComponent?.cargoOwlspPathField?.text ?: "PATH"
        state.colors = settingsComponent?.getColors() ?: mutableMapOf()
    }

    override fun reset() {
        val state = AppSettingsState.getInstance()
        settingsComponent?.cargoOwlspPathField?.text = state.cargoOwlspPath
        settingsComponent?.setColors(state.colors)
    }

    override fun disposeUIResources() {
        settingsComponent = null
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return settingsComponent?.getPreferredFocusedComponent()
    }
}
