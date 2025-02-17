package jp.s6n.idea.rustowl.configuration

import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.JBColor
import com.intellij.ui.dsl.builder.*
import java.awt.Desktop
import java.awt.Dimension
import java.net.URI
import javax.swing.*
import javax.swing.JTextField

class AppSettingsComponent {
    private val logger = Logger.getInstance(AppSettingsComponent::class.java)
    var state = RustOwlSettings.State()
    val cargoOwlspPathField = TextFieldWithBrowseButton(JTextField(getCargoOwlspPath() ?: ""))
    private val installButton = JButton("Install")
    private val colors = mutableMapOf<DecorationType, JButton>()


    val panel = panel {
        group("RustOwl Settings") {
            row("Cargo-owlsp Path:") {
                textFieldWithBrowseButton(
                    FileChooserDescriptorFactory.createSingleFileDescriptor()
                        .withTitle("Select Cargo-Owlsp Executable"),
                    null
                ) {
                    state.cargoOwlspPath = it.path
                    it.path
                }
            }

            row {
                icon(AllIcons.Vcs.Vendors.Github)
                browserLink("Open RustOwl On GitHub", GITHUB_LINK)
            }

            collapsibleGroup("Decoration Colors") {
                DecorationType.entries.forEach { type ->
                    row(type.displayText) {
                        val button = JButton().apply {
                            val savedRGB = state.colors.getOrDefault(type.colorName, type.defaultColor.rgb)
                            val color = JBColor.namedColor(type.colorName, savedRGB)
                            foreground = color
                            background = color
                            addActionListener {
                                val newColor = JColorChooser.showDialog(
                                    null,
                                    "Choose color for ${type.displayText}",
                                    color
                                )
                                if (newColor != null) {
                                    background = newColor
                                    foreground = newColor
                                    state.colors[type.colorName] = newColor.rgb
                                }
                            }
                        }
                        colors[type] = button
                        cell(button)
                            .applyToComponent {
                                preferredSize = Dimension(60, 25)
                            }
                    }
                }
            }
        }

        group("Installation") {
            row {
                cell(installButton)
                    .align(Align.FILL)
                    .applyToComponent {
                        addActionListener {
                            installCargoOwlsp()
                        }
                    }
            }
        }.visible(getCargoOwlspPath() == null)
    }

    init {
        checkCargoOwlspInPath()
    }

    private fun checkCargoOwlspInPath() {
        getCargoOwlspPath()?.let { path ->
            cargoOwlspPathField.text = path
            installButton.isEnabled = false
        } ?: run {
            cargoOwlspPathField.text = ""
            installButton.isEnabled = true
        }
    }

    private fun getCargoOwlspPath(): String? {
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
            logger.warn("Failed to detect cargo-owlsp", e)
            null
        }
    }

    private fun installCargoOwlsp() {
        installButton.isEnabled = false
        installButton.text = "Installing..."

//        SwingWorker<Boolean, Void>().execute {
//            try {
//                if (System.getProperty("os.name").startsWith("Windows")) {
//                    Desktop.getDesktop().browse(URI(BUILD_MANUAL_LINK))
//                    SwingUtilities.invokeLater {
//                        installButton.text = "Manual Install Required"
//                    }
//                } else {
//                    val process = ProcessBuilder("sh", "-c", "curl -L \"$INSTALL_SCRIPT_URL\" | sh")
//                        .redirectErrorStream(true)
//                        .start()
//
//                    if (process.waitFor() == 0) {
//                        checkCargoOwlspInPath()
//                        SwingUtilities.invokeLater {
//                            installButton.text = "Installed Successfully"
//                        }
//                    } else {
//                        throw Exception("Installation failed with exit code: ${process.waitFor()}")
//                    }
//                }
//            } catch (e: Exception) {
//                logger.error("Failed to install cargo-owlsp", e)
//                SwingUtilities.invokeLater {
//                    installButton.text = "Installation Failed"
//                    NotificationGroupManager.getInstance()
//                        .getNotificationGroup("RustOwl")
//                        .createNotification(
//                            "Failed to install cargo-owlsp",
//                            e.message ?: "Unknown error",
//                            NotificationType.ERROR
//                        )
//                        .notify(null)
//                }
//            } finally {
//                SwingUtilities.invokeLater {
//                    installButton.isEnabled = true
//                }
//            }
//            true
//        }
    }

    fun getPreferredFocusedComponent() = cargoOwlspPathField

    fun getColorFor(type: DecorationType): Int {
        return colors[type]?.background?.rgb ?: type.defaultColor.rgb
    }

    fun setColorFor(type: DecorationType, color: JBColor) {
        colors[type]?.background = color
    }

    companion object {
        private const val GITHUB_LINK = "https://github.com/cordx56/rustowl"
        private const val BUILD_MANUAL_LINK = "$GITHUB_LINK?tab=readme-ov-file#build-manually"
        private const val INSTALL_SCRIPT_URL = "$GITHUB_LINK/releases/download/latest/install.sh"
    }
}
