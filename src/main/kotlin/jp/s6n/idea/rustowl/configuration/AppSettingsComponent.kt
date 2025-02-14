package jp.s6n.idea.rustowl.configuration


import com.intellij.icons.AllIcons
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.ui.components.textFieldWithBrowseButton
import com.intellij.ui.dsl.builder.panel
import java.awt.*
import java.awt.event.ActionListener
import java.io.IOException
import java.net.URI
import javax.swing.*
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.builder.Panel

class AppSettingsComponent {

    private val logger = Logger.getInstance(this.javaClass)

    private val mainPanel: JPanel = JBPanel<JBPanel<*>>(GridBagLayout())
    val cargoOwlspPathField = TextFieldWithBrowseButton()
    private val installButton = JButton("Install")
    private val githubLink = LinkLabel<String>("RustOwl", null)
    private val colorChoosers: MutableMap<DecorationType, JButton> = mutableMapOf()
    private val isWindows = System.getProperty("os.name").startsWith("Windows")
    private val constraints = GridBagConstraints().apply {
        gridx = 0
        gridy = 0
    }

    init {

        // Label and field for cargo-owlsp path
        configureBinaryPath()

        // GitHub link
        configureGithubLink()

        // Install button
        configureInstallButton()

        configureColorPickers()

    }

    private fun configureBinaryPath() {
        JBLabel("RustOwl path:")
        val path = getCargoOwlspPath()

        panel {
            group("RustOwl Settings") {
                row("Links") {
                    text("Learn more: <icon src='AllIcons.Vcs.Vendors.Github'><a href='$GITHUB_LINK'>RustOwl</a>.")
                }

                group("Binary path") {
                    row {
                        label("Executable(cargo-owlsp) path:")
                        textFieldWithBrowseButton(
                            null,
                            JTextField(path ?: ""),
                            FileChooserDescriptorFactory.createSingleFileDescriptor()
                        ) {
                            it.path
                        }
                        if (path == null) {
                            button("Install ")
                        }
                    }
                }

                collapsibleGroup("Decoration Colors") {

                }

            }
            row {
                label("Rust Owl")
            }

        }
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

    private fun Row.installCargoOwlsp() {
        val text = if (isWindows) {
            "Manual Install Required"
        } else {
            "Install Cargo-owlsp"
        }
        button(text) {
            if (isWindows) {

            }
        }

        installButton.isEnabled = false
        installButton.text = "Installing..."

        SwingUtilities.invokeLater {
            if () {
                Desktop.getDesktop().browse(URI(BUILD_MANUAL_LINK))
                installButton.text = "Manual Install Required"
            } else {
                try {
                    val process = ProcessBuilder("sh", "-c", "curl -L \"$INSTALL_SCRIPT_URL\" | sh")
                        .redirectErrorStream(true)
                        .start()
                    process.waitFor()
                    getCargoOwlspPath()
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

    fun getPanel(): JPanel = mainPanel

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
