package io.github.frodo821.sirene.server

import java.io.OutputStream

class RemoteControlMessenger(val method: Method, val arg: String, val out: OutputStream? = null) {
    enum class Method(raw: Int) {
        PLAY(0),
        PAUSE(1),
        STOP(2),
        TONE(3),
        RESET(4),
        STATUS(5),
        LIST(6),
        INVALID(-1),
        BEGIN(-2),
        FINISH(-3)
    }

    companion object {
        val invalidMessage = RemoteControlMessenger(Method.INVALID, "")
        val beginMessage = RemoteControlMessenger(Method.BEGIN, "")
        val finishMessage = RemoteControlMessenger(Method.FINISH, "")

        fun parse(raw: String, out: OutputStream?): RemoteControlMessenger {
            val msg = raw.split('\n').toMutableList()
            val rl = msg.removeAt(0).split(' ')
            if(rl.size != 2) return invalidMessage
            if(rl[0] != "SRCP" && Method.values().all { it.toString() != rl[1] }) return invalidMessage
            return RemoteControlMessenger(Method.valueOf(rl[1]), msg.joinToString { it+"\n" }, out)
        }
    }
}