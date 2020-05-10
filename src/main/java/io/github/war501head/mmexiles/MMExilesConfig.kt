/*
 * MIT License
 *
 * Copyright (c) 2020 Sean Kelly (501warhead)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.war501head.mmexiles

import org.bukkit.Bukkit
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.logging.Logger

object MMExilesConfig {

    val log = Logger.getLogger(MMExilesConfig.javaClass.simpleName)

    var plugin: MMExiles? = null
        internal set

    var exileLocation: ExileLocation? = null
        private set
    var exileKnowsExiler = true
        private set
    var broadcastExileMessage = true
        private set
    var broadcastMessages: List<String> = emptyList()
        private set
    var remindersEnabled = true
        private set
    var reminderFrequency = 15
        private set
    val exileHandler: ExileHandler = ExileHandler()

    private var reminderTaskId: Int = 0

    fun loadConfig() {
        plugin!!.reloadConfig()
        val config = plugin!!.config
        val exileLocationFile = File(plugin!!.dataFolder, "exile_location.yml")
        if (!exileLocationFile.exists()) {
            plugin!!.saveResource("exile_location.yml", false)
        }
        val exileLocationConfig = YamlConfiguration.loadConfiguration(exileLocationFile)
        var y = exileLocationConfig.getInt("y")
        if (!(0..256).contains(y)) {
            log.warning("The specified y coordinate for the exile location is $y, which is outside of the range 0 - 256! We'll clamp but please correct this value!")
            y = y.coerceIn(0..256)
        }
        val x = exileLocationConfig.getInt("x")
        val z = exileLocationConfig.getInt("z")
        val world = exileLocationConfig.getString("world")
                ?: throw InvalidConfigurationException("Please specify a world to use for the exile location")
        exileLocation = ExileLocation(x, y, z, world)
        exileKnowsExiler = config.getBoolean("exile.knows.exiler")
        broadcastExileMessage = config.getBoolean("broadcast.enabled")
        broadcastMessages = config.getStringList("broadcast.messages")
        remindersEnabled = config.getBoolean("reminder.enabled")
        reminderFrequency = config.getInt("reminder.frequency")
    }

    fun setExileLocation(x: Int, y: Int, z: Int, world: String) {
        val locationConfigFile = File(plugin!!.dataFolder, "exile_location.yml")
        val exileLocationConfig = YamlConfiguration.loadConfiguration(locationConfigFile)
        exileLocationConfig.set("x", x)
        exileLocationConfig.set("y", y.coerceIn(0..256))
        exileLocationConfig.set("z", z)
        exileLocationConfig.set("world", world)
        exileLocationConfig.save(locationConfigFile)
        exileLocation = ExileLocation(x, y, z, world)
    }

    fun runTask() {
        if (remindersEnabled) {
            reminderTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin!!, {
                exileHandler.notifyPendingExiles()
            } as Runnable, 20 * 60 * 5, (20 * 60 * reminderFrequency).toLong())
        } else if (reminderTaskId != 0) {
            // Cancel a task if it's already running
            Bukkit.getScheduler().cancelTask(reminderTaskId)
        }
    }


}