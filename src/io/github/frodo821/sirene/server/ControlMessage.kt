package io.github.frodo821.sirene.server

class ControlMessage(val method: Method, val arg: String) {
    enum class Method(raw: Int) {
        PLAY(0),
        PAUSE(1),
        STOP(2),
        TONE(3),
        RESET(4),
        STATUS(5),
        LIST(6),
        INVALID(-1)
    }

    companion object {
        val invalidMessage = ControlMessage(Method.INVALID, "")

        fun parse(raw: String): ControlMessage {
            val msg = raw.split('\n').toMutableList()
            val rl = msg.removeAt(0).split(' ')
            if(rl.size != 2) return invalidMessage
            if(rl[0] != "SRCP" && Method.values().all { it.toString() != rl[1] }) return invalidMessage
            return ControlMessage(Method.valueOf(rl[1]), msg.joinToString { it+"\n" })
        }
    }
}