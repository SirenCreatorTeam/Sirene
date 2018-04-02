package io.github.frodo821.sirene.midi

import javax.sound.midi.*
import java.io.File
import io.github.frodo821.sirene.serial.SerialController
import kotlin.concurrent.*

class MIDILoader(file: File, controller: SerialController) {
	val sequence = MidiSystem.getSequence(file)
	val serialController = controller
	val isFinished: Boolean
		get() { return false; }
	val tracks: MutableList<Array<MidiNote>>
	init
	{
		tracks = mutableListOf<Array<MidiNote>>()
		for(track in sequence.tracks){
			tracks.add(MidiNote.createNotes({
				val list = mutableListOf<MidiEvent>()
				for(i in 0..track.size() - 1)
				{
					list.add(track.get(i))
				}
				list
			}()))
		}
	}
	
	fun playTrack(num: Int) = thread ()
	{
		for((i, t) in tracks[num].withIndex())
		{
			
		}
	}
}