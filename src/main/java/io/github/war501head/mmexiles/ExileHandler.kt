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

// We do this because for some reason they share the same class file name. IDK. Whatever, we just rename it.
import co.aikar.commands.InvalidCommandArgument
import net.md_5.bungee.api.chat.*
import org.bukkit.Bukkit
import org.bukkit.ChatColor.*
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*
import java.util.stream.Collectors
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import net.md_5.bungee.api.ChatColor as MD5Color

class ExileHandler {

    data class ExileRequest(val exiler: UUID, val exile: UUID, val reason: String)

    var exileRequests: List<ExileRequest> = emptyList()
        private set

    fun requestExile(exilier: Player, exile: OfflinePlayer, reason: String): Status {
        if (exileRequests.any { exileRequest -> exileRequest.exile == exile.uniqueId }) {
            return Status.PENDING_EXILE
        }
        exileRequests = exileRequests + ExileRequest(exilier.uniqueId, exile.uniqueId, reason)
        notifyPendingExiles()
        return Status.SUCCESS
    }

    /**
     * Notifies everyone with the "exile.admin" permission of pending exiles.
     */
    fun notifyPendingExiles() {
        if (exileRequests.isEmpty()) {
            return
        }
        Bukkit.getOnlinePlayers()
                .stream()
                .filter { player: Player -> player.hasPermission("exile.admin") }
                .collect(Collectors.toList())
                .forEach { player ->
                    val textComponent = TextComponent("There are ${exileRequests.size} pending exile requests, M'lord. Click to review.")
                    textComponent.color = net.md_5.bungee.api.ChatColor.GRAY
                    textComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder("Click to view pending Exile Requests").create())
                    textComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/exile list")
                    player.spigot().sendMessage(textComponent)
                }
    }

    /**
     * Confirms the exile of a person
     */
    fun confirmExile(exile: Player): Status {
        if (exileRequests.none { exileRequest -> exileRequest.exile == exile.uniqueId }) {
            return Status.NOT_FOUND
        }
        val exileRequest: ExileRequest = exileRequests.first { exileRequest -> exileRequest.exile == exile.uniqueId }
        val exiler = Bukkit.getOfflinePlayer(exileRequest.exiler)
        val reason = exileRequest.reason
        doExile(exile, exiler.name, reason)
        exileRequests = exileRequests - exileRequest
        exiler.player?.sendMessage("${GOLD}${ITALIC}Your prayers have been answered and ${exile.name} has been exiled from these lands...")
        return Status.SUCCESS
    }

    fun rejectExile(exile: Player): Status {
        if (exileRequests.none { exileRequest -> exileRequest.exile == exile.uniqueId }) {
            return Status.NOT_FOUND
        }
        val exileRequest: ExileRequest = exileRequests.first { exileRequest -> exileRequest.exile == exile.uniqueId }
        val exiler = Bukkit.getOfflinePlayer(exileRequest.exiler)
        exiler.player?.sendMessage("${RED}Your request to exile ${exile.displayName} $GRAY(${exile.name})$RESET$RED has been rejected!")
        exileRequests = exileRequests - exileRequest
        return Status.SUCCESS
    }

    fun doExile(exile: Player, exiler: String?, reason: String) {
        val exileLocation = MMExilesConfig.exileLocation?.toLocation()
                ?: throw InvalidCommandArgument("Exile location was not configured properly. Use /exile location to set this up")
        exile.teleport(exileLocation)
        if (MMExilesConfig.exileKnowsExiler) {
            exile.sendMessage("${RED}You have been exiled! Your accuser, $exiler, reported you for $RESET$GRAY$reason")
        } else {
            exile.sendMessage("${RED}You have been exiled! Think on your actions...")
        }
        if (MMExilesConfig.broadcastExileMessage) {
            Bukkit.broadcastMessage(getBroadcastMessage(exile, reason))
        }
        var team = Bukkit.getScoreboardManager()?.mainScoreboard?.getTeam("exiled")
        if (team == null) {
            team = Bukkit.getScoreboardManager()?.mainScoreboard?.registerNewTeam("exiled")
                    ?: throw InvalidCommandArgument("Failed to find or register the team Exiled. Probably needs an OP (Dunkel) to fix")
            team.color = GOLD
            team.suffix = "$GOLD the Exiled"
        }
        team.addEntry(exile.name)
    }

    fun setExileLocation(loc: Location) {
        MMExilesConfig.setExileLocation(loc.blockX, loc.blockY, loc.blockZ, loc.world!!.name)
    }

    private fun getBroadcastMessage(exile: Player, reason: String): String {
        val message = MMExilesConfig.broadcastMessages.shuffled().take(1)[0]
                .replace("{PLAYER}", "$RESET$RED${exile.displayName} $GRAY(${exile.name})$RESET$RED}")
                .replace("{REASON}", "$RESET${DARK_RED}$reason$RESET$RED")
        return "$RED$message"
    }

    fun getMaxPages(): Int {
        return max(ceil(exileRequests.size.toDouble() / 3.toDouble()).toInt(), 1)
    }

    fun buildExileList(page: Int): Map<Array<BaseComponent>, String> {
        if (exileRequests.isEmpty()) {
            return emptyMap()
        }
        val components = mutableMapOf<Array<BaseComponent>, String>()
        // Pagination algorithm
        val offset = page * 3
        exileRequests.slice(offset..min(offset + 2, exileRequests.size - 1)).forEach { request ->
            val exile = Bukkit.getOfflinePlayer(request.exile)
            val exiler = Bukkit.getOfflinePlayer(request.exiler)
            val component = ComponentBuilder()
            component.append(">> ").color(MD5Color.DARK_AQUA)
            if (exile.isOnline) {
                component.append(exile.name).color(MD5Color.GREEN)
            } else {
                component.append(exile.name).color(MD5Color.RED)
            }
            component.append(" - By: ").color(MD5Color.DARK_AQUA)
            if (exiler.isOnline) {
                component.append(exiler.name).color(MD5Color.GREEN)
            } else {
                component.append(exiler.name).color(MD5Color.RED)
            }
            component.append(" - ").color(MD5Color.DARK_AQUA)
            val exileButton = TextComponent("[EXILE]")
            exileButton.color = MD5Color.DARK_RED
            exileButton.isBold = true
            exileButton.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(TextComponent("Click to Exile!")).create())
            exileButton.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/exile approve ${exile.name}")
            component.append(exileButton).append(" ").reset()
            val pardonButton = TextComponent("[PARDON]")
            pardonButton.color = MD5Color.DARK_GREEN
            pardonButton.isBold = true
            pardonButton.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(TextComponent("Click to pardon")).create())
            pardonButton.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/exile reject ${exile.name}")
            component.append(pardonButton)
            components[component.create()] = request.reason
        }
        return components
        /*
        >> 501warhead - Request By: Theryn - [APPROVE] [REJECT]
        He took my shit and I think he needs a time out. Please hurry! He's being really mean to me!
         */
    }

    enum class Status {
        PENDING_EXILE, SUCCESS, NOT_FOUND
    }
}