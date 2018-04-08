package io.github.frodo821.sirene.serial

import java.io.IOException
import java.io.OutputStream
import gnu.io.SerialPort
import gnu.io.CommPortIdentifier
import gnu.io.CommPort

/**
 * Controls serial connection.
 */
class SerialController(portn: String)
{
	companion object
	{
		const val BPS = 9600
	}
	
	val PORT = portn
	val port: SerialPort
	val output: OutputStream
	
	init
	{
		val comId = CommPortIdentifier.getPortIdentifier(PORT)
		val cport = comId.open("Sirene Controller", 2000)
		port = cport as SerialPort
		port.setSerialPortParams(
				BPS,
				SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1,
				SerialPort.PARITY_NONE)
		port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE)
		output = port.getOutputStream()
	}
	
	fun write(data: String): Boolean
	{
		return write(data.toByteArray())
	}
	
	fun write(data: ByteArray): Boolean
	{
		try
		{
			output.write(data)
			//println("SEND [${data.toString(Charsets.US_ASCII)}]")
			return true
		}
		catch(err: IOException)
		{
			return false
		}
	}
	
	fun close()
	{
		write("28")
		output.close()
		port.close()
	}
}