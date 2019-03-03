package io.github.frodo821.sirene

import io.github.frodo821.sirene.application.AppMain
import io.github.frodo821.sirene.configuration.Config
import javafx.application.Application
import java.io.File

private var __config_cache: Config? = null
private val __cfile = File(File(System.getProperty("user.home"), ".sirene"), "config.yml")

fun main(args: Array<String>)
{
	Application.launch(AppMain::class.java, *args)
}

fun getConfig(reload: Boolean = false): Config {
    if(!__cfile.exists()) {
        if(!__cfile.parentFile.exists())
            __cfile.parentFile.mkdirs()
        __config_cache = Config(mutableMapOf())
        __config_cache?.save(__cfile)
    } else if(reload || __config_cache == null)
        __config_cache = Config.parseFile(__cfile)
    return __config_cache!!
}

fun saveConfig() {
    __config_cache?.save(__cfile)
}
