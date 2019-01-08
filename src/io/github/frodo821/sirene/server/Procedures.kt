package io.github.frodo821.sirene.server

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.frodo821.sirene.getConfig
import java.io.File
import java.io.PrintWriter

object Constants {
    val extensions = listOf(".mid", ".smf")
}

fun setupProcedures() {
    val defaultMethod: Procedure = fun(it, _, _, _) {
        it.responseHeaders.add("Content-Type", "text/plain")
        it.sendResponseHeaders(403, 0)
        PrintWriter(it.responseBody).use {out ->
            out.println("Forbidden")
        }
    }
    Server.RPCs["play"] = fun (it, _, body, ctl) {
        val music = solveMusic(body)
        if(music == null) {
            it.requestHeaders.add("Content-Type", "application/json")
            it.sendResponseHeaders(404, 0)
            val mapper = ObjectMapper()
            PrintWriter(it.responseBody).use { out ->
                out.println(mapper.writeValueAsString(SRCP.PLAY_MUSIC_NOT_FOUND))
            }
            return
        }
        try {
            ctl.playMusic(music)
            it.requestHeaders.add("Content-Type", "application/json")
            it.sendResponseHeaders(200, 0)
            val mapper = ObjectMapper()
            PrintWriter(it.responseBody).use { out ->
                out.println(mapper.writeValueAsString(SRCP.PLAY_SUCCEEDED))
            }
        } catch (e: Exception) {
            it.requestHeaders.add("Content-Type", "application/json")
            it.sendResponseHeaders(500, 0)
            val mapper = ObjectMapper()
            PrintWriter(it.responseBody).use { out ->
                out.println(mapper.writeValueAsString(SRCP.PLAY_FAILED))
            }
            e.printStackTrace()
        }
    }
    Server.RPCs["list"] = fun (it, _, _, _) {
        val dir = File(getConfig().get("music_directory", System.getProperty("user.home")))
        val musics = dir
                .listFiles {d -> d.isFile && Constants.extensions.contains("." + d.extension.toLowerCase())}
                .map { it.nameWithoutExtension }

        it.responseHeaders.add("Content-Type", "application/json")
        it.sendResponseHeaders(200, 0)
        PrintWriter(it.responseBody).use {out ->
            val mapper = ObjectMapper()
            mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true)
            out.println(mapper.writeValueAsString(ListResponse(SRCP.LIST_SUCCEEDED, musics)))
        }
    }
    Server.RPCs["index"] = defaultMethod
    Server.RPCs["status"] = defaultMethod
    Server.RPCs["stop"] = defaultMethod
    Server.RPCs["pause"] = defaultMethod
    Server.RPCs["tone"] = defaultMethod
}

fun solveMusic(music: String): File? {
    val extensions = mutableListOf(".mid", ".smf")
    for (e in extensions) {
        val f = File(getConfig().get("music_directory", System.getProperty("user.home")), music+e)
        if(f.exists())
            return f
    }
    return null
}
