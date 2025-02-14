package jp.s6n.idea.rustowl.configuration

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil
import java.awt.Color

@State(name = "CargoOwlspSettings", storages = [Storage("cargoOwlspSettings.xml")])
class AppSettingsState : PersistentStateComponent<AppSettingsState> {
    var cargoOwlspPath: String = "PATH"
    var colors: MutableMap<DecorationType, Color> = mutableMapOf()

    override fun getState(): AppSettingsState = this

    override fun loadState(state: AppSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        fun getInstance(): AppSettingsState = service()
    }
}
