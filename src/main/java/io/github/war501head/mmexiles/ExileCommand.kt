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

import co.aikar.commands.BaseCommand
import co.aikar.commands.CommandHelp
import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import net.md_5.bungee.api.chat.*
import org.bukkit.ChatColor.*
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player
import net.md_5.bungee.api.ChatColor as MD5Color

@CommandAlias("exile|ex")
class ExileCommand(private val handler: ExileHandler) : BaseCommand() {

    @Default
    @CommandPermission("exile.use")
    @CommandCompletion("@players @nothing")
    @Description("Requests for another player to be exiled")
    fun requestExile(commandSender: CommandSender, player: OnlinePlayer, reason: String) {
        if (commandSender.hasPermission("exile.admin")) {
            handler.doExile(player.player, (commandSender as? Player)?.name ?: "Console", reason)
        } else {
            when (handler.requestExile(commandSender as Player, player.player, reason)) {
                ExileHandler.Status.PENDING_EXILE -> commandSender.sendMessage("$RED[Error]$GRAY That player already has an outstanding request for exile")
                ExileHandler.Status.SUCCESS -> commandSender.sendMessage("${GREEN}You've requested that ${player.player.name} be exiled! Wait for your request to be reviewed...")
                ExileHandler.Status.NOT_FOUND -> commandSender.sendMessage("$RED[Error]$GRAY The player ${player.player.name} was not found. Are they online?")
            }
        }
    }

    @HelpCommand
    @CatchUnknown
    fun onHelp(sender: CommandSender?, help: CommandHelp) {
        help.showHelp()
    }

    @Subcommand("approve")
    @CommandPermission("exile.admin")
    @Description("Approves a pending exile")
    fun approveExile(commandSender: CommandSender, player: OnlinePlayer) {
        val result = handler.confirmExile(player.player)
        if (result != ExileHandler.Status.SUCCESS) {
            when (result) {
                ExileHandler.Status.NOT_FOUND -> throw InvalidCommandArgument("No pending request to exile ${player.player.name} was found")
                else -> throw InvalidCommandArgument("An unknown error happened. Try again.")
            }
        }

    }

    @Subcommand("reject")
    @CommandPermission("exile.admin")
    @Description("Rejects a pending exile")
    fun rejectExile(commandSender: CommandSender, player: OnlinePlayer) {
        val result = handler.rejectExile(player.player)
        if (result != ExileHandler.Status.SUCCESS) {
            when (result) {
                ExileHandler.Status.NOT_FOUND -> throw InvalidCommandArgument("No pending request to exile ${player.player.name} was found")
                else -> throw InvalidCommandArgument("An unknown error happened. Try again.")
            }
        }
    }

    @Subcommand("list")
    @CommandPermission("exile.admin")
    @Description("Lists all exile requests and gives action buttons")
    fun listPendingExiles(commandSender: CommandSender, @Default("1") page: Int) {
        if (page < 1) {
            throw InvalidCommandArgument("Page value ($page) cannot be negative or zero")
        }
        val maxPages = handler.getMaxPages()
        if (page > maxPages) {
            throw InvalidCommandArgument("Page $page is larger than the total pages, which is $maxPages")
        }
        val exileList: Map<Array<BaseComponent>, String> = handler.buildExileList(page - 1)
        commandSender.sendMessage("$RED.:: $BOLD PENDING MAGISTRATIVE REVIEW$RESET$RED ::.")
        if (handler.exileRequests.isEmpty()) {
            commandSender.sendMessage("${GRAY}There are no pending requests M'lord, check back later perhaps?")
        } else {
            if (commandSender is ConsoleCommandSender) {
                exileList.forEach { (exileButtons, exileReason) ->
                    commandSender.sendMessage(TextComponent(*exileButtons).toPlainText())
                    commandSender.sendMessage("$GRAY$ITALIC$exileReason")
                }
                return
            }
            // We need the * as a spread operator so that this method will take what we're giving it
            exileList.forEach { (exileButtons, exileReason) ->
                commandSender.spigot().sendMessage(*exileButtons)
                commandSender.sendMessage("$GRAY$ITALIC$exileReason")
            }
        }
        commandSender.spigot().sendMessage(*buildPaginationBottom(page, maxPages))
    }

    @Subcommand("reload")
    @CommandPermission("exile.admin")
    @Description("Reloads the configuration for this plugin")
    fun reloadPlugin(sender: CommandSender) {
        MMExilesConfig.loadConfig()
        sender.sendMessage("${GREEN}Successfully reloaded the configuration for the plugin")
    }

    @Subcommand("location")
    @CommandPermission("exile.admin")
    @Description("Sets the location that new exiles are exiled to")
    fun setLocation(sender: Player) {
        val loc = sender.location
        handler.setExileLocation(loc)
        sender.sendMessage("${GREEN}The target exile location has now been set to ${MMExilesConfig.exileLocation}")
    }

    private fun buildPaginationBottom(page: Int, maxPage: Int): Array<BaseComponent> {
        val componentBuilder = ComponentBuilder()
        if (page == 1) {
            componentBuilder.append("≪ ").color(MD5Color.GRAY)
        } else {
            val component = TextComponent("≪")
            component.color = MD5Color.AQUA
            component.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(TextComponent("Back")).create())
            component.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/exile list ${page - 1}")
            componentBuilder.append(component).append(" ")
        }
        componentBuilder.append("Page ").color(MD5Color.DARK_AQUA)
                .append("$page").color(MD5Color.GOLD)
                .append("/$maxPage ").color(MD5Color.DARK_AQUA)
        if (page >= maxPage) {
            componentBuilder.append("≫").color(MD5Color.GRAY)
        } else {
            val component = TextComponent("≫")
            component.color = MD5Color.AQUA
            component.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(TextComponent("Forward")).create())
            component.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/exile list ${page + 1}")
            componentBuilder.append(component)
        }
        return componentBuilder.create()
    }

}