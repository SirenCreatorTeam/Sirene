package io.github.frodo821.sirene.application

import javafx.application.Application
import javafx.stage.Stage
import javafx.scene.Scene
import javafx.scene.Parent
import javafx.fxml.FXMLLoader
import io.github.frodo821.sirene.constants

class AppMain: Application()
{
	override fun start(primaryStage: Stage)
	{
		primaryStage.title = "${constants.appName} - ${constants.version}"
		primaryStage.scene = Scene(
				FXMLLoader.load(this.javaClass.getResource("MainUI.fxml")), 600.0, 400.0)
		primaryStage.show()
	}
}