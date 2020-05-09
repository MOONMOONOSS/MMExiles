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

import co.aikar.commands.PaperCommandManager
import org.bukkit.plugin.java.JavaPlugin

class MMExiles : JavaPlugin() {

    override fun onEnable() {
        // Plugin startup logic
        saveDefaultConfig()
        MMExilesConfig.plugin = this
        MMExilesConfig.loadConfig()
        val manager = PaperCommandManager(this)
        manager.enableUnstableAPI("help")
        manager.registerCommand(ExileCommand(MMExilesConfig.exileHandler))

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    override fun onLoad() {
        MMExilesConfig.plugin = this
    }
}