package io.github.frodo821.sirene.midi

import kotlin.math.round
import javax.sound.midi.*
import java.nio.ByteBuffer

class MidiNote(note: Int, start: Long, end: Long) {
  companion object {
    private const val FPT = 1024 //Frames per tick

    fun createNotes(track: Track, resolution: Int, debug: Boolean = false): Array<MidiNote> {
      if (debug)
        println("NOTE_ON = ${ShortMessage.NOTE_ON}, NOTE_OFF = ${ShortMessage.NOTE_OFF}")

      val zero = 0.toByte()

      val pairs = mutableMapOf<Byte, MidiEvent>()

      val ret = mutableListOf<MidiNote>()

      var mspt: Double = 500000.toDouble() / FPT

      for (i in 0 until track.size()) {
        val t = track.get(i)
        if (t.message.status == 0xFF && t.message.message[1].toInt() == 0x51) {
          if (t.message.message.lastIndex != 5)
            throw java.lang.IllegalStateException("Invalid byte length: ${t.message.message.lastIndex}")
          mspt = ((t.message.message[3].toInt() shl 16) + (t.message.message[4].toInt() shl 8) + t.message.message[5].toInt()).toDouble() / FPT
        }

        if (t.message.message.lastIndex != 2)
          continue

        if (t.message.status == ShortMessage.NOTE_ON &&
          t.message.message[2] != zero) {
          if (pairs.containsKey(t.message.message[1]))
            continue
          pairs[t.message.message[1]] = t
          continue
        }

        if (t.message.status == ShortMessage.NOTE_OFF ||
          (t.message.status == ShortMessage.NOTE_ON && t.message.message[2] == zero)) {
          val e = pairs.remove(t.message.message[1]) ?: continue
          if (debug) {
            val note = MidiNote(t.message.message[1].toInt(), round(e.tick.toDouble() / resolution * mspt).toLong(), round(t.tick.toDouble() / resolution * mspt).toLong())
            println("${(i - 1) / 2}: $note")
            ret.add(note)
          } else {
            ret.add(MidiNote(t.message.message[1].toInt(), round(e.tick.toDouble() / resolution * mspt).toLong(), round(t.tick.toDouble() / resolution * mspt).toLong()))
          }
        }
      }
      return ret.toTypedArray()
    }
  }

  val note = note
  val start = start
  val end = end

  override fun toString(): String {
    return "[note = $note, startTick = $start, endTick = $end]"
  }
}
