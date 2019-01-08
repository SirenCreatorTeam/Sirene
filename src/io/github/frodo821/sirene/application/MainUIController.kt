@file:Suppress("NullableBooleanElvis")

package io.github.frodo821.sirene.application

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ToggleButton
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.Label
import javafx.scene.canvas.*
import javafx.application.Platform
import javafx.fxml.Initializable
import io.github.frodo821.sirene.serial.SerialController
import io.github.frodo821.sirene.midi.MidiLoader
import io.github.frodo821.sirene.constants
import io.github.frodo821.sirene.application.ui.KeyboardController
import java.util.ResourceBundle
import java.net.URL
import javafx.scene.control.ToggleGroup
import javafx.stage.FileChooser
import java.io.File
import gnu.io.NoSuchPortException
import io.github.frodo821.sirene.getConfig
import io.github.frodo821.sirene.saveConfig
import io.github.frodo821.sirene.server.Server
import javafx.event.Event
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService

class MainUIController : Initializable {
    @FXML
    lateinit var musicName: Label
    @FXML
    lateinit var selectAuto: ToggleButton
    @FXML
    lateinit var playMusic: ToggleButton
    @FXML
    lateinit var chooseMusic: Button
    @FXML
    lateinit var selectRemote: ToggleButton
    @FXML
    lateinit var connectStatus: Label
    @FXML
    lateinit var connectIndicator: ProgressIndicator
    @FXML
    lateinit var keyboard: Canvas
    private var file: File? = null
    private var performThread: MutableList<Thread> = mutableListOf()
    private var loader: MidiLoader? = null
    private val group = ToggleGroup()
    val controllers = mutableListOf<SerialController>()
    private lateinit var service: ExecutorService
    private lateinit var keyctl: KeyboardController
    private var remoteServer: Server? = null
    private val config = getConfig()

    init {
        AppMain.onCloseHandlers.add {
            saveConfig()
            onApplicationClosed()
        }
        try {
            controllers.addAll(SerialController.getAvailablePorts())
        } catch (exc: NoSuchPortException) {
        }
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        service = Executors.newFixedThreadPool(5)
        selectAuto.toggleGroup = group
        selectRemote.toggleGroup = group
        selectAuto.isSelected = true
        musicName.text = "音楽を選択してください"
        connectStatus.text = "未接続です"
        playMusic.isDisable = true
        autoPlay()
        keyctl = KeyboardController(keyboard, this)
        keyctl.draw()
        selectAuto.setOnAction {
            if (selectAuto.isSelected) {
                autoPlay()
            }
            if (!selectAuto.isSelected && !selectRemote.isSelected) {
                selectAuto.isSelected = true
            }
        }
        selectRemote.setOnAction {
            if (selectRemote.isSelected) {
                remotePlay()
            }
            if (!selectAuto.isSelected && !selectRemote.isSelected) {
                selectRemote.isSelected = true
            }
        }
        chooseMusic.setOnAction { chooseMusic() }
        playMusic.setOnAction {
            if (playMusic.isSelected) {
                play()
            } else {
                chooseMusic.isDisable = false
                playMusic.text = "演奏する"
                performThread.forEach { th ->
                    try {
                        th.interrupt()
                    } catch (exc: Exception) {
                    }
                }
                performThread.clear()
                println("Thread stopped")
                keyctl.draw()
            }
        }
    }

    private fun play() {
        playMusic.text = "演奏停止"
        val ldr = loader ?: return run()
        {
            playMusic.text = "演奏する"
            chooseMusic.isDisable = false
            playMusic.isSelected = false
        }
        ldr.playingCallbacks.add { note -> println(note) }
        ldr.playingCallbacks.add { note -> Platform.runLater { keyctl.highlight(note) } }
        val th = Thread()
        {
            Platform.runLater()
            {
                chooseMusic.isDisable = true
            }
            performThread.addAll(ldr.playTracks(0 to 0, 1 to 1))
            performThread.forEach { it.join() }
            println("Returned to UI thread")
            Platform.runLater()
            {
                playMusic.text = "演奏する"
                chooseMusic.isDisable = false
                playMusic.isSelected = false
            }
        }
        service.execute(th)
    }

    private fun autoPlay() {
        onApplicationClosed(false)
        connectIndicator.isDisable = true
        chooseMusic.isDisable = false
        val f = file
        if (f != null) {
            chooseMusic.text = "選択中 ${f.nameWithoutExtension}"
            musicName.text = f.nameWithoutExtension
            playMusic.isDisable = false
            if (controllers.isEmpty())
                try {
                    controllers.addAll(SerialController.getAvailablePorts())
                } catch (exc: NoSuchPortException) {
                    println(constants.PortNotFound)
                    return
                }
            loader = MidiLoader(f, controllers)
        }
    }

    private fun remotePlay() {
        onApplicationClosed(false)
        connectIndicator.progress = ProgressIndicator.INDETERMINATE_PROGRESS
        connectIndicator.isDisable = false
        playMusic.isDisable = true
        chooseMusic.isDisable = true
        val svr = Server(
                config.get("host", "0.0.0.0"),
                config.get("port", 12549),
                this@MainUIController)
        svr.onClosed = {
                Event.fireEvent(selectAuto, MouseEvent(
                        MouseEvent.MOUSE_CLICKED,
                        .0, .0, .0, .0,
                        MouseButton.PRIMARY, 1,
                        true, true, true, true, true, true,
                        true, true, true, true, null))
        }
        remoteServer = svr
    }

    fun playMusic(music: File) {
        chooseMusic.text = "選択中 ${music.nameWithoutExtension}"
        musicName.text = music.nameWithoutExtension
        file = music
        if (controllers.isEmpty())
            try {
                controllers.addAll(SerialController.getAvailablePorts())
            } catch (exc: NoSuchPortException) {
                return
            }
        loader = MidiLoader(music, controllers)
        play()
    }

    private fun chooseMusic() {
        val fc = FileChooser()
        fc.title = "演奏するMIDIファイルを選択..."
        fc.extensionFilters.add(
                FileChooser.ExtensionFilter("Midi シーケンスファイル", "*.mid", "*.smf"))
        fc.initialDirectory = if (file == null) File(System.getProperty("user.home")) else file?.parentFile
        val f = fc.showOpenDialog(chooseMusic.scene.window)
        if (f != null) {
            chooseMusic.text = "選択中 ${f.nameWithoutExtension}"
            musicName.text = f.nameWithoutExtension
            file = f
            playMusic.isDisable = false
            if (controllers.isEmpty())
                try {
                    controllers.addAll(SerialController.getAvailablePorts())
                } catch (exc: NoSuchPortException) {
                    return
                }
            loader = MidiLoader(f, controllers)
        } else if (file == null) {
            playMusic.isDisable = true
        }
    }

    private fun onApplicationClosed(disposeControllers: Boolean = true) {
        if (disposeControllers)
            println("User requested to this to quit...")
        if (remoteServer != null) {
            runBlocking {
                remoteServer?.close()
                println("Server was successfully shutdown.")
            }
        }
        println("Interrupting recorder performing task...")
        performThread.forEach {
            try {
                it.interrupt()
            } catch (exc: Exception) {
            }
        }
        println("All performers are successfully killed.")
        performThread.clear()
        if (disposeControllers) {
            println("disposing controllers...")
            controllers.forEach { it.close() } //TODO("特定条件下で無限に待機する不具合を修正する")
            println("Shutdown completed. See you!")
        }
    }
}