package io.github.frodo821.sirene.midi

import javax.sound.midi.*
import java.io.File
import io.github.frodo821.sirene.serial.SerialController
import io.github.frodo821.sirene.constants

class MidiLoader(file: File, controller: List<SerialController>) {
	private val sequence = MidiSystem.getSequence(file)
	private val serialController = controller.toTypedArray()
	val isFinished: Boolean
		get() { return false; }
	private val tracks: MutableList<Array<MidiNote>> = mutableListOf()
    val playingCallbacks = mutableListOf<(Int) -> Unit>()

	init {
        for(track in sequence.tracks){
			tracks.add(MidiNote.createNotes(track, sequence.resolution))
		}
	}
	
	fun playTracks(vararg num: Pair<Int, Int>) = num.map()
	{
		Thread()
		{
			try
			{
				playTrack(it.first, it.second)
			}
			catch(e: IndexOutOfBoundsException) { }
		}
	}.also{ t -> t.forEach { it.start() } }
	
	private fun playTrack(num: Int, portIndex: Int = -1)
	{
		val port = if(portIndex == -1) num else portIndex
		val track = tracks[num]
		if(port > serialController.lastIndex)
		{
			println("WARNING: Port index {num} not present!")
			return
		}
		for((i, t) in track.withIndex())
		{
			if(i != 0)
				Thread.sleep(if(t.start > track[i - 1].end) t.start - track[i - 1].end else 0)
			serialController[port].write("${t.note - constants.BOTTOM_NOTE}".padStart(2, '0'))
			playingCallbacks.forEach { it(t.note) }
		    Thread.sleep(if(t.end > t.start) t.end - t.start else 0)
			serialController[port].write("28")
			playingCallbacks.forEach { it(-1) }
			while(serialController[port].input.available() > 0)
			{
				serialController[port].input.read()
			}
		}
	}
}