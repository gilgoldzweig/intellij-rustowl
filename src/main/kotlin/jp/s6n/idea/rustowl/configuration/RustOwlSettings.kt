package jp.s6n.idea.rustowl.configuration

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.ui.JBColor
import com.intellij.util.xml.Attribute
import com.intellij.util.xml.ConvertContext
import com.intellij.util.xml.Converter
import com.intellij.util.xmlb.annotations.MapAnnotation
import com.intellij.util.xmlb.annotations.XMap
import org.jetbrains.annotations.NonNls
import java.awt.Color

//@State(name = "RustOwlSettings", storages = [Storage("rustOwlSettings.xml")])
//class RustOwlSettings : SimplePersistentStateComponent<RustOwlSettings.State>() {
//    var cargoOwlspPath: String = ""
//    var colors: MutableMap<DecorationType, Color> = mutableMapOf()
//
//    override fun getState(): RustOwlSettings = this
//
//    override fun loadState(state: RustOwlSettings.State) {
//        TODO("Not yet implemented")
//    }
//
//    override fun loadState(state: RustOwlSettings) {
//        XmlSerializerUtil.copyBean(state, this)
//    }
//
//    companion object {
//        fun getInstance(): RustOwlSettings = service()
//    }
//
//    class State : BaseState() {
//        var cargoOwlspPath: String by string(getCargoOwlspPath())
//
//    }
//}
//
//fun getCargoOwlspPath(): String? {
////    val logger = Logger.getInstance()
//    val command = if (System.getProperty("os.name").startsWith("Windows")) {
//        "where cargo-owlsp"
//    } else {
//        "which cargo-owlsp"
//    }
//
//    return try {
//        val process = ProcessBuilder(command.split(" "))
//            .redirectErrorStream(true)
//            .start()
//
//        if (process.waitFor() == 0) {
//            process.inputStream.bufferedReader().readLine()
//        } else {
////            logger.error("Failed to get path from input stream")
//            null
//        }
//    } catch (e: IOException) {
////        logger.error("Failed to get path from input stream", e)
//        null
//    }
//}

@Service
@State(name = "RustOwlSettings", storages = [Storage("rustOwlSettings.xml")])
class RustOwlSettings : SimplePersistentStateComponent<RustOwlSettings.State>(State()) {
    private val logger = Logger.getInstance(RustOwlSettings::class.java)

    @Transient
    private var colorCache: MutableMap<DecorationType, JBColor> = mutableMapOf()

    var cargoOwlspPath: String
        get() {
            val path = state.cargoOwlspPath
            if (path.isNullOrEmpty()) {
                val detectedPath = detectCargoOwlspPath()
                    ?: throw BinaryNotFoundException("cargo-owlsp not found in PATH. Please install it first.")
                state.cargoOwlspPath = detectedPath
                return detectedPath
            }
            return path
        }
        set(value) {
            if (value.isEmpty()) {
                throw IllegalArgumentException("cargoOwlspPath cannot be empty")
            }
            state.cargoOwlspPath = value
        }

    fun getColor(type: DecorationType): JBColor {
        return colorCache.getOrPut(type) {
            state.colors.getOrDefault(type.colorName, type.defaultColor)
            type.defaultColor
        }
    }

    fun setColor(type: DecorationType, color: JBColor) {
        colorCache[type] = color
        state.colors[type.colorName] = color.rgb
    }

    fun resetColor(type: DecorationType) {
        colorCache.remove(type)
        state.colors[type.colorName] = type.defaultColor.rgb
    }

    fun resetAllColors() {
        colorCache.clear()
        state.colors.clear()
    }

    private fun detectCargoOwlspPath(): String? {
        val command = if (System.getProperty("os.name").startsWith("Windows")) {
            "where cargo-owlsp"
        } else {
            "which cargo-owlsp"
        }

        return try {
            val process = ProcessBuilder(command.split(" "))
                .redirectErrorStream(true)
                .start()


            if (process.waitFor() == 0) {
                process.inputStream.bufferedReader().readLine()
            } else {
                logger.warn("cargo-owlsp not found in PATH")
                null
            }
        } catch (e: Exception) {
            logger.warn("Failed to detect cargo-owlsp path", e)
            null
        }
    }

    class State : BaseState() {
        var cargoOwlspPath by string("")

        @get:XMap
        var colors by map<String, Int>()
    }

    class BinaryNotFoundException(message: String) : Exception(message)

    companion object {
        fun getInstance(): RustOwlSettings = service()
    }
}
