package io.github.frodo821.sirene.serial

import java.io.IOException
import java.io.OutputStream
import java.io.InputStream
import gnu.io.SerialPort
import gnu.io.CommPortIdentifier
import gnu.io.PortInUseException

/**
 * Controls serial connection.
 */
class SerialController(portn: String) {
    companion object {
        const val BPS = 9600

        fun getAvailablePorts(): Array<SerialController> {
            val ports = mutableListOf<SerialController>()
            //TODO("Sireneが使っているわけではないCOMポートが検知される不具合を修正する")
            for (p in CommPortIdentifier.getPortIdentifiers()) {
                if (p is CommPortIdentifier) {
                    println("Available Port: ${p.name}")
                    try {
                        ports.add(SerialController(p.name))
                    } catch (exc: PortInUseException) {
                    }
                }
            }
            return ports.toTypedArray()
        }
    }

    val PORT = portn
    val port: SerialPort
    val output: OutputStream
    val input: InputStream

    init {
        val comId = CommPortIdentifier.getPortIdentifier(PORT)
        val cport = comId.open("Sirene Controller", 2000)
        port = cport as SerialPort
        port.setSerialPortParams(
                BPS,
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE)
        port.flowControlMode = SerialPort.FLOWCONTROL_NONE
        output = port.outputStream
        input = port.inputStream
    }

    fun write(data: String): Boolean {
        return write(data.toByteArray())
    }

    fun write(data: ByteArray): Boolean {
        try {
            output.write(data)
            output.write(".".toByteArray())
            //println("SEND [${data.toString(Charsets.US_ASCII)}]")
            return true
        } catch (err: IOException) {
            return false
        }
    }

    fun close() {
        write("28")
        output.close()
        port.close()
    }
}