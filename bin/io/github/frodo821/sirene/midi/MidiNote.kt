package io.github.frodo821.sirene.midi

import javax.sound.midi.*
import java.nio.ByteBuffer

class MidiNote(note: Int, start: Long, end: Long) {
	companion object
	{
		fun createNotes(track: List<MidiEvent>): Array<MidiNote>
		{
			val zero = 0.toByte()
			val pairs = mutableMapOf<Byte, MidiEvent>()
			val ret = mutableListOf<MidiNote>()
			for(t in track)
			{
				if(t.message.message.lastIndex != 2)
					continue
				if(t.message.status == ShortMessage.NOTE_ON &&
				   t.message.message[2] != zero)
				{
					if(pairs.containsKey(t.message.message[1]))
						continue
					pairs.put(t.message.message[1], t)
					continue
				}
				if(t.message.status == ShortMessage.NOTE_OFF ||
				   t.message.message[2] == zero)
				{
					val e = pairs.get(t.message.message[1])
					if(e == null)
						continue
					ret.add(MidiNote(t.message.message[1].toInt(), e.tick, t.tick))
				}
			}
			return ret.toTypedArray()
		}
	}
	
	val note = note
	val start = start
	val end = end
	
	override fun toString(): String
	{
		return "[note = ${note}, startTick = ${start}, endTick = ${end}]"
	}
}