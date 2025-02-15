package jp.s6n.idea.rustowl.configuration

import com.intellij.icons.AllIcons
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.labels.LinkLabel
import jp.s6n.idea.rustowl.configuration.DecorationType
import java.awt.*
import java.io.IOException
import java.net.URI
import com.intellij.ui.dsl.builder.*
import javax.swing.*


class AppSettingsComponent {
    private val logger  = Logger.getInstance(AppSettingsComponent::class.java)
    private val mainPanel: JPanel = JBPanel<JBPanel<*>>(GridBagLayout())
    val cargoOwlspPathField = TextFieldWithBrowseButton()
    private val installButton = JButton("Install")
    private val githubLink = LinkLabel<String>("RustOwl", null)
    private val colorChoosers: MutableMap<DecorationType, JButton> = mutableMapOf()
    private val colorSettings = mutableMapOf<DecorationType, Color>()


    private val constraints = GridBagConstraints().apply {
        gridx = 0
        gridy = 0
    }

    val panel = panel {
        val path = getCargoOwlspPath()
        group("RustOwl Settings") {
            row("Cargo-owlsp Path:") {
                com.intellij.ui.components.textFieldWithBrowseButton(
                    null,
                    JTextField(path ?: ""),
                    FileChooserDescriptorFactory.createSingleFileDescriptor()
                ) {
                    it.path
                }
            }
            row {
                icon(AllIcons.Vcs.Vendors.Github)
                browserLink("Open RustOwl on GitHub", GITHUB_LINK)
            }
            collapsibleGroup("Decoration Colors") {
                DecorationType.entries.forEach { type ->
                    row(type.displayText) {
                        val colorButton = JButton()
                        colorButton.background = colorSettings.getOrDefault(type, type.defaultColor)
                        colorButton.addActionListener {
                            val newColor = JColorChooser.showDialog(null, "Choose color for ${type.displayText}", colorButton.background)
                            if (newColor != null) {
                                colorButton.background = newColor
                                colorSettings[type] = newColor
                            }
                        }
                        cell(colorButton)
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
        }
    }

    init {

//        // Label and field for cargo-owlsp path
//        configureBinaryPath()
//
//        // GitHub link
//        configureGithubLink()
//
//        // Install button
//        configureInstallButton()
//
//        configureColorPickers()

    }

    private fun configureBinaryPath() {

        mainPanel.add(JBLabel("RustOwl Path:"), constraints)
        constraints.gridx = 1
        cargoOwlspPathField.text = "PATH"
        mainPanel.add(cargoOwlspPathField, constraints)
        // Check if cargo-owlsp is in PATH
        checkCargoOwlspInPath()
    }

    private fun configureGithubLink() {
        constraints.gridx = 0
        constraints.gridy = 1
        mainPanel.add(JBLabel("More info:"), constraints)
        constraints.gridx = 1
        githubLink.setListener(
            { _, _ ->
                Desktop.getDesktop().browse(URI(GITHUB_LINK))
            },
            null
        )
        mainPanel.add(githubLink, constraints)
    }

    private fun configureInstallButton() {
        constraints.gridx = 0
        constraints.gridy = 2
        mainPanel.add(JBLabel("Install cargo-owlsp:"), constraints)
        constraints.gridx = 1
        installButton.addActionListener {
            installCargoOwlsp()
        }
        mainPanel.add(installButton, constraints)
    }

    private fun configureColorPickers() {
        DecorationType.entries.forEachIndexed { index, type ->
            constraints.gridx = 0
            constraints.gridy = index + 1
            mainPanel.add(JLabel(type.displayText), constraints) // Use displayText

            val button = JButton().apply {
                background = type.defaultColor
                addActionListener { chooseColor(this, type) }
            }
            colorChoosers[type] = button

            constraints.gridx = 1
            mainPanel.add(button, constraints)
        }
    }

    private fun chooseColor(button: JButton, type: DecorationType) {
        val newColor = JColorChooser.showDialog(null, "Choose color for ${type.displayText}", button.background)
        if (newColor != null) {
            button.background = newColor
        }
    }

    private fun checkCargoOwlspInPath() {
        val command = if (System.getProperty("os.name").startsWith("Windows")) {
            "where cargo-owlsp"
        } else {
            "which cargo-owlsp"
        }

        try {
            val process = ProcessBuilder(command.split(" "))
                .redirectErrorStream(true)
                .start()
            val exitCode = process.waitFor()
            if (exitCode == 0) {
                val path = process.inputStream.bufferedReader().readLine()
                cargoOwlspPathField.text = path
                installButton.isEnabled = false
            } else {
                cargoOwlspPathField.text = "Not found"
                installButton.isEnabled = true
            }
        } catch (e: IOException) {
            cargoOwlspPathField.text = "Error checking PATH"
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
                logger.error("Failed to get path from input stream")
                null
            }
        } catch (e: IOException) {
            logger.error("Failed to get path from input stream", e)
            null
        }
    }

    private fun installCargoOwlsp() {
        installButton.isEnabled = false
        installButton.text = "Installing..."

        SwingUtilities.invokeLater {
            if (System.getProperty("os.name").startsWith("Windows")) {
                Desktop.getDesktop().browse(URI(BUILD_MANUAL_LINK))
                installButton.text = "Manual Install Required"
            } else {
                try {
                    val process = ProcessBuilder("sh", "-c", "curl -L \"$INSTALL_SCRIPT_URL\" | sh")
                        .redirectErrorStream(true)
                        .start()
                    process.waitFor()
                    checkCargoOwlspInPath()
                    installButton.text = "Installed Successfully"
                } catch (e: IOException) {
                    installButton.text = "Installation Failed"
                    Notifications.Bus.notify(
                        Notification(
                            "RustOwl",
                            "Failed to install RustOwlsp",
                            e.message ?: "Unknown error",
                            NotificationType.ERROR
                        )
                    )
                }
            }
            installButton.isEnabled = true
        }
    }

    fun getPanel(): JPanel = panel

    fun getPreferredFocusedComponent() = cargoOwlspPathField

    fun getColors(): MutableMap<DecorationType, Color> =
        colorChoosers.mapValues { it.value.background }.toMutableMap()

    fun setColors(newColors: MutableMap<DecorationType, Color>) {
        newColors.forEach { (type, color) -> colorChoosers[type]?.background = color }
    }
}

private const val GITHUB_LINK = "https://github.com/cordx56/rustowl"
private const val BUILD_MANUAL_LINK = "$GITHUB_LINK?tab=readme-ov-file#build-manually"
private const val INSTALL_SCRIPT_URL = "$GITHUB_LINK/releases/download/latest/install.sh"
