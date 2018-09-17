package io.github.frodo821.sirene.configuration

import java.io.File
import java.io.IOException
import org.ho.yaml.Yaml

@Suppress("UNCHECKED_CAST")
class Config(config: Map<String, Any>)
{
    companion object
    {
        fun parseFile(file: File): Config
        {
            return Config(Yaml.load(file) as Map<String, Any>)
        }
    }
    private val config: MutableMap<String, Any>
	private val blocks: MutableList<String>
	
    init
    {
		val map = mutableMapOf<String, Any>()
		val blocks = mutableListOf<String>()
		for((k, c) in config)
		{
			if(c is Map<*, *>)
			{
				map.put(k, Config(c as Map<String, Any>))
				blocks.add(k)
				continue
			}
			map.put(k, c)
		}
		this.config = map
		this.blocks = blocks
    }
	
	fun hasBlock(key: String): Boolean = blocks.contains(key)
	
	fun hasItem(key: String): Boolean = config.containsKey(key)
	
	fun <T: Any> get(key: String, def: T): T
	{
		val item = config.get(key)
		if(item == null)
		{
			when(def)
			{
				is Config -> {
					config.put(key, def)
					blocks.add(key)
				}
				is Map<*, *> -> {
					config.put(key, Config(def as Map<String, Any>))
					blocks.add(key)
				}
				else -> {
					config.put(key, def)
				}
			}
		}
		return config.get(key)!! as T
	}
	
	fun <T: Any> get(key: String): T?
	{
		return config.get(key) as T?
	}
	
	fun save(file: File)
	{
		Yaml.dump(config, file, true)
	}
}
