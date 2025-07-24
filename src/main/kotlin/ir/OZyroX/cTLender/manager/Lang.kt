package ir.OZyroX.cTLender.manager

import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.*
import org.bukkit.ChatColor

object Lang {
    private val messages = mutableMapOf<String, Any>()
    private var currentLang = "en"
    var prefix = ""

    fun load(plugin: JavaPlugin) {
        val configLang = plugin.config.getString("language") ?: "en"
        currentLang = configLang

        val langFile = File(plugin.dataFolder, "lang/$configLang.yml")
        if (!langFile.exists()) {
            plugin.saveResource("lang/$configLang.yml", false)
        }
        plugin.logger.info("${configLang} Language Loaded!")

        val yaml = YamlConfiguration.loadConfiguration(langFile)
        for (key in yaml.getKeys(true)) {
            val value = yaml.get(key)
            when (value) {
                is String -> messages[key] = value
                is List<*> -> messages[key] = value.filterIsInstance<String>()
            }
        }

        prefix = yaml.getString("prefix") ?: ""
    }



    fun get(key: String, vararg args: Pair<String, String>): String {
        val value = messages[key]
        if (value !is String) return key

        var msg = value
        args.forEach { (k, v) -> msg = (msg as String).replace("{$k}", v) }
        return ChatColor.translateAlternateColorCodes('&', msg as String)
    }


    fun getList(key: String, vararg args: Pair<String, String>): List<String> {
        val value = messages[key]
        if (value !is List<*>) return listOf(key)

        return value.filterIsInstance<String>().map { line ->
            var msg = line
            args.forEach { (k, v) -> msg = msg.replace("{$k}", v) }
            ChatColor.translateAlternateColorCodes('&', msg)
        }
    }




}

