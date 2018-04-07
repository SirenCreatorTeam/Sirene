package io.github.frodo821.sirene.application

import javafx.fxml.FXML
import kotlinx.coroutines.experimental.*
import javafx.scene.control.Button
import javafx.scene.control.ToggleButton
import javafx.scene.control.MenuButton
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.Label
import javafx.scene.canvas.*
import javafx.scene.paint.*
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
import javax.sound.midi.MidiSystem
import javafx.stage.Window
import io.github.frodo821.sirene.configuration.Config
import gnu.io.NoSuchPortException

class MainUIController: Initializable
{
	@FXML lateinit var musicName: Label
	@FXML lateinit var selectAuto: ToggleButton
	@FXML lateinit var playMusic: ToggleButton
	@FXML lateinit var chooseMusic: Button
	@FXML lateinit var selectRemote: ToggleButton
	@FXML lateinit var connectStatus: Label
	@FXML lateinit var connectIndicator: ProgressIndicator
	@FXML lateinit var keyboard: Canvas
	private var file: File? = null
	private var performJob: Job? = null
	private var loader: MidiLoader? = null
	private val group = ToggleGroup()
	private val config: Config
	private val controllers = mutableListOf<SerialController>()
	private lateinit var keyctl: KeyboardController
	init
	{
		AppMain.onCloseHandlers.add { OnApplicationClosed() }
		val cfg = File("${System.getenv("APPDATA").replace("\\", "/")}/${constants.CompanyName}/${constants.appName.replace(" ", "")}/settings.yml".replace("//", "/"))
		if(!cfg.exists())
		{
			if(!cfg.parentFile.exists())
				cfg.parentFile.mkdirs()
			cfg.createNewFile();
			config = Config(mutableMapOf())
		}
		else
		{
			config = Config.parseFile(cfg)
		}
		val port = config.get("port", "COM3")
		config.save(cfg)
		try
		{
			controllers.add(SerialController(port))
		}
		catch(exc: NoSuchPortException)
		{ }
	}
	
	override fun initialize(location: URL?, resources: ResourceBundle?)
	{
		selectAuto.setToggleGroup(group)
		selectRemote.setToggleGroup(group)
		selectAuto.setSelected(true)
		musicName.setText("音楽を選択してください")
		connectStatus.setText("未接続です")
		playMusic.setDisable(true)
		autoPlay()
		keyctl = KeyboardController(keyboard)
		keyctl.draw()
		selectAuto.setOnAction()
		{
			if(selectAuto.isSelected())
			{
				autoPlay()
			}
			if(!selectAuto.isSelected() && !selectRemote.isSelected())
			{
				selectAuto.setSelected(true)
			}
		}
		selectRemote.setOnAction()
		{
			if(selectRemote.isSelected())
			{
				remotePlay()
			}
			if(!selectAuto.isSelected() && !selectRemote.isSelected())
			{
				selectRemote.setSelected(true)
			}
		}
		chooseMusic.setOnAction()
		{
			chooseMusic()
		}
		playMusic.setOnAction()
		{
			if(playMusic.isSelected())
			{
				performJob = play()
			}
			else
			{
				chooseMusic.setDisable(false)
				playMusic.setText("演奏する")
				performJob?.cancel()
				keyctl.draw()
				println("Performance canceled")
			}
		}
		println("Initialized!")
	}
	
	private fun play(): Job?
	{
		playMusic.setText("演奏停止")
		val ldr = loader ?: return null
		ldr.playingCallbacks.add {note -> println(note)}
		ldr.playingCallbacks.add {note -> Platform.runLater {keyctl.higilight(note)}}
		return launch()
		{
			chooseMusic.setDisable(true)
			ldr.playTrack(0)
			playMusic.setText("演奏する")
			chooseMusic.setDisable(false)
			playMusic.setSelected(false)
		}
	}
	
	private fun autoPlay()
	{
		println("AutoPlay mode selected!")
		connectIndicator.setDisable(true)
		chooseMusic.setDisable(false)
		val f = file
		if(f != null) {
			chooseMusic.setText("選択中 ${f.nameWithoutExtension}")
			musicName.setText(f.nameWithoutExtension)
			playMusic.setDisable(false)
			if(controllers.isEmpty())
				try
				{
					controllers.add(SerialController(config.get<String>("port")!!))
				}
				catch(exc: NoSuchPortException)
				{
					println(constants.PortNotFound)
					return
				}
			loader = MidiLoader(f, controllers[0])
		}
	}
	
	private fun remotePlay()
	{
		println("RemotePlay mode selected!")
		connectIndicator.progress = ProgressIndicator.INDETERMINATE_PROGRESS;
		connectIndicator.setDisable(false)
		playMusic.setDisable(true)
		chooseMusic.setDisable(true)
	}
	
	private fun chooseMusic()
	{
		val fc = FileChooser()
		fc.setTitle("演奏するMIDIファイルを選択...")
		fc.getExtensionFilters().add(
				FileChooser.ExtensionFilter("Midi シーケンスファイル", "*.mid", "*.smf"))
		fc.setInitialDirectory(
				if(file == null) File(System.getProperty("user.home"))
				else file?.parentFile)
		val f = fc.showOpenDialog(chooseMusic.getScene().getWindow())
		if(f != null)
		{
			chooseMusic.setText("選択中 ${f.nameWithoutExtension}")
			musicName.setText(f.nameWithoutExtension)
			file = f
			playMusic.setDisable(false)
			if(controllers.isEmpty())
				try
				{
					controllers.add(SerialController(config.get<String>("port")!!))
				}
				catch(exc: NoSuchPortException)
				{
					println(constants.PortNotFound)
					return
				}
			loader = MidiLoader(f, controllers[0])
		}
		else if(file == null)
		{
			playMusic.setDisable(true)
		}
	}
	
	private fun OnApplicationClosed()
	{
		performJob?.cancel()
		controllers.forEach { it.close() }
		println("Closed all controllers.")
	}
}