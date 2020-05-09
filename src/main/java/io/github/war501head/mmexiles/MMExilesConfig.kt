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