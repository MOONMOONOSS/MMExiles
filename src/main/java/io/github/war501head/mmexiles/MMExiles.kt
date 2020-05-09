package io.github.war501head.mmexiles

import co.aikar.commands.PaperCommandManager
import io.github.war501head.mmexiles.command.ExileCommand
import io.github.war501head.mmexiles.handler.ExileHandler
import org.bukkit.plugin.java.JavaPlugin

class MMExiles : JavaPlugin() {

    private var exileHandler: ExileHandler? = null

    override fun onEnable() {
        // Plugin startup logic
        saveDefaultConfig()
        MMExilesConfig.plugin = this
        MMExilesConfig.loadConfig()
        val manager = PaperCommandManager(this)
        manager.enableUnstableAPI("help")
        exileHandler = MMExilesConfig.exileLocation?.let { ExileHandler(it) }
        manager.registerCommand(ExileCommand(exileHandler!!))
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    override fun onLoad() {
        MMExilesConfig.plugin = this
    }
}