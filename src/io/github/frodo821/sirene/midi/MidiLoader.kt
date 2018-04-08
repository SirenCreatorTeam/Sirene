package io.github.frodo821.sirene.midi

import javax.sound.midi.*
import java.io.File
import io.github.frodo821.sirene.serial.SerialController
import io.github.frodo821.sirene.constants
import kotlinx.coroutines.experimental.*

class MidiLoader(file: File, controller: SerialController) {
	val sequence = MidiSystem.getSequence(file)
	val serialController = controller
	val isFinished: Boolean
		get() { return false; }
	val tracks: MutableList<Array<MidiNote>>
	val playingCallbacks = mutableListOf<(Int) -> Unit>()
	init
	{
		tracks = mutableListOf<Array<MidiNote>>()
		for(track in sequence.tracks){
			tracks.add(MidiNote.createNotes(track, sequence.resolution))
		}
	}
	
	suspend fun playTrack(num: Int)
	{
		val track = tracks[num]
		for((i, t) in track.withIndex())
		{
			if(i != 0)
				delay(t.start - track[i - 1].end)
			serialController.write("${t.note - constants.BOTTOM_NOTE}".padStart(2, '0'))
			playingCallbacks.forEach { it(t.note) }
			if(i != track.lastIndex)
				delay(t.end - t.start)
			serialController.write("28")
			playingCallbacks.forEach { it(-1) }
		}
	}
}