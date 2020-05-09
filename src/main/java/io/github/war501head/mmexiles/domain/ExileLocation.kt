package io.github.war501head.mmexiles.domain

import org.bukkit.Bukkit
import org.bukkit.Location

data class ExileLocation(val x: Int, val y: Int, val z: Int, val world: String) {

    fun toLocation(): Location {
        val world = Bukkit.getWorld(world)
        if (world == null) {
            //throw exception
            throw Exception()
        }
        // Add 0.5 otherwise they'll end up at the corner of the block, small details
        return Location(world, x.toDouble() + 0.5, y.toDouble(), z.toDouble() + 0.5)
    }
}