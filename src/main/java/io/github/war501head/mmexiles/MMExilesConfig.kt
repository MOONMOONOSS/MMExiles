package io.github.war501head.mmexiles

import io.github.war501head.mmexiles.domain.ExileLocation
import org.bukkit.configuration.InvalidConfigurationException

object MMExilesConfig {

    var plugin: MMExiles? = null
        internal set

    var exileLocation: ExileLocation? = null
        private set
    var exileKnowsExiler = true
        private set
    var broadcastExileMessage = true
        private set

    fun loadConfig() {
        val config = plugin!!.config
        val x = config.getInt("exile.target.x")
        val y = config.getInt("exile.target.y")
        if (!(0..256).contains(y)) throw InvalidConfigurationException("The y coordinate for the exile location must be between 0 and 256. Specified: $y")
        val z = config.getInt("exile.target.z")
        val world = config.getString("exile.target.world")
                ?: throw InvalidConfigurationException("Please specify a world to use for the exile location")
        exileLocation = ExileLocation(x, y, z, world)
    }
}