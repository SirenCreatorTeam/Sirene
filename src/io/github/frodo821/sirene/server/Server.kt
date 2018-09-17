package io.github.frodo821.sirene.server

import java.nio.*
import java.net.*
import io.reactivex.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import java.nio.channels.ServerSocketChannel
import java.nio.charset.Charset

class Server(val backlog: Int = 2048): AutoCloseable {

    companion object {
        const val EOT = '\u0004'
    }

    private val server: ServerSocketChannel = ServerSocketChannel.open()
    private var _running = false
    val messager: Observable<ControlMessage>
    private val emitters = mutableListOf<Emitter<ControlMessage>>()

    init {
        messager = Observable.create { emitters.add(it) }
    }

    fun accept() = async(CommonPool) {
        try {
            server.socket().bind(InetSocketAddress(4567))
            _running = true
            serve()
            true
        }catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun serve()
    {
        val cache = mutableListOf<Pair<String, Boolean>>()
        while (true) {
            val buffer = ByteBuffer.allocate(backlog)
            val s = server.accept()
            if(s.read(buffer) < 0) continue
            buffer.flip()
            val str = Charset.forName("UTF-8").decode(buffer).toString()
            val requests = str.split(EOT).toMutableList()
            val lasti = cache.lastIndex

            if(!cache[lasti].second)
            {
                val first = (cache.last().first + requests.removeAt(0))
                emitters.forEach { it.onNext(ControlMessage.parse(first)) }
                cache[lasti] = first to true
            }

            val last = requests.removeAt(requests.lastIndex)

            requests.forEach { item ->
                cache.add(item to true)
                emitters.forEach { it.onNext(ControlMessage.parse(item)) }
            }

            if(str.endsWith(EOT)) {
                cache.add(last to true)
                emitters.forEach { last }
            } else {
                cache.add(last to false)
            }
        }
    }

    override fun close() {
        server.close()
        _running = false
        emitters.forEach { it.onComplete() }
    }
}