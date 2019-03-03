package io.github.frodo821.sirene.application.ui

import javafx.scene.canvas.*
import javafx.scene.paint.*
import io.github.frodo821.sirene.constants
import io.github.frodo821.sirene.application.MainUIController
import javafx.scene.input.MouseEvent
import io.github.frodo821.sirene.serial.SerialController
import gnu.io.NoSuchPortException
import javafx.scene.input.MouseButton

class KeyboardController(kbd: Canvas, uictl: MainUIController) {
	companion object
	{
		const val BLACK_KEY = 0
		const val WHITE_KEY = 1
		const val KEY_SIZE = 32.0
		const val PADDING = 1.0
	}
	
	private val controller = uictl
	private val keyboard = kbd
	private val context = kbd.graphicsContext2D
	private val keyRectos = mutableListOf<Rect>()
	private val keys = listOf(
			WHITE_KEY,
				BLACK_KEY,
			WHITE_KEY,
				BLACK_KEY,
			WHITE_KEY,
			
			WHITE_KEY,
				BLACK_KEY,
			WHITE_KEY,
				BLACK_KEY,
			WHITE_KEY,
				BLACK_KEY,
			WHITE_KEY,
			
			WHITE_KEY,
				BLACK_KEY,
			WHITE_KEY,
				BLACK_KEY,
			WHITE_KEY,
			
			WHITE_KEY,
				BLACK_KEY,
			WHITE_KEY,
				BLACK_KEY,
			WHITE_KEY,
				BLACK_KEY,
			WHITE_KEY,
			
			WHITE_KEY,
				BLACK_KEY,
			WHITE_KEY)
	init
	{
		keyboard.setOnMousePressed()
		{
			if(it.button == MouseButton.PRIMARY)
				OnMousePressed(it)
		}
		keyboard.setOnMouseReleased()
		{
			if(it.button == MouseButton.PRIMARY)
				OnMouseReleased()
		}
	}
	
	fun draw()
	{
		keyRectos.clear()
		context.clearRect(0.0 ,0.0 , keyboard.width, keyboard.height)
		context.stroke = Color.BLACK;
		context.lineWidth = 1.0
		var side = (keyboard.width - (KEY_SIZE + PADDING * 2) * 16) / 2
		for((i, k) in keys.withIndex())
		{
			if(k == WHITE_KEY)
			{
				val rect = Rect(side, 50.0, KEY_SIZE, 150.0)
				keyRectos.add(rect)
				context.strokeRect(rect.x, rect.y, rect.w, rect.h)
			}
			else
			{
				val rect = Rect(side, 50.0, KEY_SIZE, 100.0)
				keyRectos.add(rect)
				context.fillRect(rect.x, rect.y, rect.w, rect.h)
			}
			side += (KEY_SIZE / 2 + PADDING) * (if(i%12 == 11 || i%12 == 4) 2 else 1)
		}
	}
	
	fun drawBlack()
	{
		if(keyRectos.isEmpty() || keyRectos.lastIndex != keys.lastIndex)
			return
		context.fill = Color.BLACK;
		for((i, k) in keys.withIndex())
		{
			if(k == WHITE_KEY) continue
			val rect = keyRectos[i]
			context.fillRect(rect.x, rect.y, rect.w, rect.h)
		}
	}
	
	fun highlight(key: Int)
	{
		if(key == -1)
		{
			draw()
			return
		}
		val kr = keyRectos.getOrNull(key - constants.BOTTOM_NOTE) ?: return
		context.fill = Color.BLUE;
		context.fillRect(kr.x, kr.y, kr.w, kr.h)
		context.fill = Color.BLACK;
		if(kr.h == 150.0) drawBlack()
	}
	
	private fun OnMousePressed(it: MouseEvent)
	{
		if(controller.controllers.isEmpty())
			try
			{
				controller.controllers.addAll(SerialController.getAvailablePorts())
			}
			catch(exc: NoSuchPortException)
			{
				//println(Constants.PortNotFound)
				return
			}
		if(controller.controllers.isEmpty())
		{
			//println("No port available.")
			return
		}
		if(controller.playMusic.isSelected)
		{
			//println("AutoPlay mode enabled.")
			return
		}
		var inRect: Rect? = null
		var index = 0
		for((i, r) in keyRectos.withIndex())
		{
			if(r.isIncluding(it.x, it.y) && (inRect == null || r.h == 100.0))
			{
				inRect = r
				index = i
			}
		}
		if(inRect != null)
		{
			highlight(index + constants.BOTTOM_NOTE)
			controller.controllers[0].write("${index}".padStart(2, '0'))
		}
	}
	
	private fun OnMouseReleased()
	{
		if(controller.controllers.isEmpty())
			try
			{
				controller.controllers.addAll(SerialController.getAvailablePorts())
			}
			catch(exc: NoSuchPortException)
			{
				//println(Constants.PortNotFound)
				return
			}
		if(controller.controllers.isEmpty())
		{
			//println("No port available.")
			return
		}
		if(controller.playMusic.isSelected)
		{
			//println("AutoPlay mode enabled.")
			return
		}
		highlight(-1)
		controller.controllers[0].write("28")
	}
}