package io.github.frodo821.sirene.midi

import io.github.frodo821.sirene.constants
import io.github.frodo821.sirene.serial.SerialController

class Performer(
  private val controller: SerialController,
  private val playingCallbacks: List<(Int) -> Unit>) {
  private val _notes: MutableList<MidiNote> = mutableListOf()

  var notes: List<MidiNote>
    set(value) {
      _notes.clear()
      _notes.addAll(value)
      _done = false
    }
    get() = _notes

  private var _done: Boolean = false

  val done
    get() = _done

  fun tick(tick: Long) {
    if(done) return
    val t = _notes[0]
    when (tick) {
      t.start -> {
        val note = t.note - constants.BOTTOM_NOTE
        if(note >= 0)
          controller.write("$note".padStart(2, '0'))
        playingCallbacks.forEach { it(-1) }
        readAll()
      }
      t.end -> {
        _notes.removeAt(0)
        _done = _notes.isEmpty()
        controller.write("28")
        readAll()
      }
    }
  }

  fun pause() {
    controller.write("28")
  }

  fun resume() {
    val note = _notes[0].note - constants.BOTTOM_NOTE
    if(note >= 0)
      controller.write("$note".padStart(2, '0'))
  }

  private fun readAll(): ByteArray {
    val ints = mutableListOf<Int>()
    while(controller.input.available() > 0)
    {
      ints.add(controller.input.read())
    }
    val ret = ByteArray(ints.lastIndex+1)
    ints.forEachIndexed {it, idx -> ret[idx] = it.toByte() }
    return ret
  }
}