package io.github.frodo821.sirene.server

import io.reactivex.Emitter
import io.reactivex.Observable
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.SocketException
import java.nio.charset.Charset

class Server: AutoCloseable {

    companion object {
        const val EOT = '\u0004'
    }

    private val server: ServerSocket = ServerSocket()
    private var _running = false
    val messager: Observable<RemoteControlMessenger>
    private val emitters = mutableListOf<Emitter<RemoteControlMessenger>>()

    init {
        messager = Observable.create { emitters.add(it) }
    }

    fun accept() = async(CommonPool) {
        try {
            server.bind(InetSocketAddress("0.0.0.0",4567))
            _running = true
            serve()
            true
        }catch (e: Exception) {
            if(e is SocketException) {
                println("User cancelled while waiting client to serve.")
                true
            } else {
                e.printStackTrace()
                false
            }
        }
    }

    private fun serve()
    {
        val cache = mutableListOf<Pair<String, Boolean>>()
        val s = server.accept()
        val ins = s.getInputStream()
        val out = s.getOutputStream()
        emitters.forEach { it.onNext(RemoteControlMessenger.beginMessage) }

        while (s.isConnected) {
            if(ins.available() < 1) continue

            val str = ins.readBytes(ins.available()).toString(Charset.forName("UTF-8"))
            val requests = str.split(EOT).toMutableList()
            val lasti = cache.lastIndex

            if(!cache[lasti].second)
            {
                val first = (cache.last().first + requests.removeAt(0))
                emitters.forEach { it.onNext(RemoteControlMessenger.parse(first, out)) }
                cache[lasti] = first to true
            }

            val last = requests.removeAt(requests.lastIndex)

            requests.forEach { item ->
                cache.add(item to true)
                emitters.forEach { it.onNext(RemoteControlMessenger.parse(item, out)) }
            }

            if(str.endsWith(EOT)) {
                cache.add(last to true)
                emitters.forEach { it.onNext(RemoteControlMessenger.parse(last, out)) }
            } else {
                cache.add(last to false)
            }
        }
        emitters.forEach { it.onNext(RemoteControlMessenger.finishMessage) }
    }

    override fun close() {
        server.close()
        _running = false
        emitters.forEach { it.onComplete() }
    }
}