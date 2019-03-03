package io.github.frodo821.sirene.midi

import io.github.frodo821.sirene.serial.SerialController
import kotlinx.coroutines.*
import java.io.File
import javax.sound.midi.MidiSystem
import javax.sound.midi.Sequence

class PerformanceManager(controller: List<SerialController>) {
  companion object {
    const val framerate = 32
  }
  private val performers = mutableListOf<Performer>();
  private var exitRequested = false
  private var paused = false
  val callbacks = mutableListOf<(Int) -> Unit>()
  lateinit var sequence: Sequence

  init {
    performers.addAll(controller.map { Performer(it, callbacks) })
  }

  fun setMidiFile(file: File) {
    sequence = MidiSystem.getSequence(file)
    performers
      .zip(sequence.tracks)
      .forEach { it.first.notes = MidiNote.createNotes(it.second, sequence.resolution * 1000 / framerate).toList() }
  }

  fun reset() {
    performers
      .zip(sequence.tracks)
      .forEach { it.first.notes = MidiNote.createNotes(it.second, sequence.resolution * 1000 / framerate).toList() }
  }

  fun stop() {
    exitRequested = true
  }

  fun pause() {
    performers.forEach { it.pause() }
    paused = true
  }

  fun resume() {
    performers.forEach { it.resume() }
    paused = false
  }

  private fun interrupt() = GlobalScope.launch {
    while(paused) {
      Thread.sleep(1000L / framerate)
    }
  }

  fun perform() = GlobalScope.launch {
    var cnt = 0L
    while (!performers.all { it.done }) {
      if(cnt != 0L)
        Thread.sleep(1000L / framerate)
      performers.forEach { it.tick(cnt) }
      if(exitRequested)
        return@launch
      interrupt().join()
      cnt++
    }
  }
}