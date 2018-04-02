package io.github.frodo821.sirene.application

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ToggleButton
import javafx.scene.control.MenuButton
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.Label
import javafx.scene.canvas.*
import javafx.scene.paint.*
import javafx.fxml.Initializable
import io.github.frodo821.sirene.serial.SerialController
import java.util.ResourceBundle
import java.net.URL
import javafx.scene.control.ToggleGroup
import javafx.stage.FileChooser
import java.io.File
import javax.sound.midi.MidiSystem
import javafx.stage.Window

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
	private val group = ToggleGroup()
	
	override fun initialize(location: URL?, resources: ResourceBundle?)
	{
		selectAuto.setToggleGroup(group)
		selectRemote.setToggleGroup(group)
		selectAuto.setSelected(true)
		musicName.setText("音楽を選択してください")
		connectStatus.setText("未接続です")
		playMusic.setDisable(true)
		autoPlay()
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
		println("Initialized!")
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
		}
		else if(file == null)
		{
			playMusic.setDisable(true)
		}
	}
}