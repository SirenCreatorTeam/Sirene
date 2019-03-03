package io.github.frodo821.sirene.server

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import io.github.frodo821.sirene.application.MainUIController
import javafx.application.Platform
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
/* import kotlinx.coroutines.* */
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.nio.charset.Charset
import java.util.concurrent.Executors

class Server(val host: String, val port: Int, private val ctl: MainUIController): AutoCloseable {
    companion object {
        val RPCs = mutableMapOf<String, Procedure>()
    }

    private val server: HttpServer = HttpServer.create(InetSocketAddress(host, port), 0)
    private val httpThreadPool = Executors.newFixedThreadPool(8)
    private val serverJob: Job
    private val __onClosed = mutableListOf<()->Unit>()
    var onClosed: () -> Unit = {}
        set (value) {__onClosed.add(value)}
    val isAlive get() = serverJob.isActive

    init {
        server.apply {
            executor = httpThreadPool
            createContext("/", fun(it: HttpExchange) {
                if(it.requestMethod.toUpperCase() != "POST") {
                    it.responseHeaders.add("Content-Type", "text/plain")
                    it.sendResponseHeaders(400, 0)
                    PrintWriter(it.responseBody).use { out ->
                        out.println("Expected method is POST, but received ${it.requestMethod}")
                    }
                    return
                }

                val segs = it.requestURI.toString().split('/').filter { k -> k.isNotEmpty() }
                val body = it.requestBody.readBytes().toString(Charset.forName("UTF-8"))
                val rpc = RPCs[if(segs.isNotEmpty()) segs[0] else "index"]

                if(rpc == null) {
                    it.responseHeaders.add("Content-Type", "text/plain")
                    it.sendResponseHeaders(404, 0)
                    PrintWriter(it.responseBody).use { out ->
                        out.println("Unknown RPC Method: ${segs[0]}")
                    }
                    return
                }

                rpc(it, segs.drop(1), body, ctl)
            })
        }

        setupProcedures()
        serverJob = GlobalScope.async { start() }
    }

    private fun start() {
        Platform.runLater {
            ctl.connectStatus.text = "Listening on $host:$port..."
        }
        server.start()
    }

    override fun close() {
        Platform.runLater {
            ctl.connectStatus.text = "未接続です"
        }
        server.stop(1)
        httpThreadPool.shutdown()
        runBlocking {
            serverJob.cancel()
            serverJob.join()
        }
        __onClosed.forEach { it() }
        __onClosed.clear()
    }

    override fun toString() = "<SRCP Server> [$host:$port]"
}