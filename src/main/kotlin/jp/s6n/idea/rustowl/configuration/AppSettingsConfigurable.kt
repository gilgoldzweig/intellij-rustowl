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
        return settingsComponent!!.panel
    }

    override fun isModified(): Boolean {
        val component = settingsComponent
        val state = RustOwlSettings.getInstance().state

        return state.cargoOwlspPath != component?.cargoOwlspPathField?.text ||
            state.colors != component?.state?.colors
    }

    override fun apply() {
        val component = settingsComponent?.state
        val state = RustOwlSettings.getInstance().state
        state.cargoOwlspPath = component?.cargoOwlspPath ?: ""
        state.colors = component?.colors ?: mutableMapOf()
    }

    override fun reset() {
        val component = settingsComponent?.state
        val state = RustOwlSettings.getInstance().state
        component?.cargoOwlspPath = state.cargoOwlspPath ?: ""
        component?.colors = state.colors
    }

    override fun disposeUIResources() {
        settingsComponent = null
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return settingsComponent?.getPreferredFocusedComponent()
    }
}
