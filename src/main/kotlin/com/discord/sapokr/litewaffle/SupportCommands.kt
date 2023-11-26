package com.discord.sapokr.litewaffle

import com.discord.sapokr.litewaffle.LiteWaffle.Companion.ActiveListeners
import com.discord.sapokr.litewaffle.LiteWaffle.Companion.Jda
import com.discord.sapokr.litewaffle.LiteWaffle.Companion.MsgListener
import com.discord.sapokr.litewaffle.LiteWaffle.Companion.cfg
import com.discord.sapokr.litewaffle.LiteWaffle.Companion.getStatus
import com.discord.sapokr.litewaffle.LiteWaffle.Companion.isDebug
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

val LWCommandArg = mutableListOf<String>("refresh", "listeners", "debug", "info")
val booleans = mutableListOf<String>("true", "false")
class SupportCommands: CommandExecutor {

    private fun refreshListener(listener: ListenerAdapter) {
        if (MsgListener.contains(listener)) {
            MsgListener.remove(listener)
            MsgListener.add(listener)
        } else {
            Jda!!.removeEventListener(listener)
            Jda!!.addEventListener(listener)
        }
    }
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("""
                ${ChatColor.GOLD}LiteWaffle Commands
                ${ChatColor.YELLOW}${LWCommandArg.joinToString(", ")}
            """.trimIndent())
            return false
        }
        val message: String = "${ChatColor.GOLD}[LiteWaffle] ${ChatColor.YELLOW}" + when(args[0]) {
            "refresh" -> {
                if(args.size > 1) {
                    if (ActiveListeners.isEmpty()) {
                        "No Listener Registered."
                    } else if(ActiveListeners.keys.contains(args[1])) {
                        ActiveListeners[args[1]]!!.listeners.forEach {
                            refreshListener(it)
                        }
                        "${ChatColor.GREEN}${args[1]} Refresh Completed."
                    } else if (args[1].lowercase() == "confirm") {
                        ActiveListeners.keys.forEach {
                            ActiveListeners[it]!!.listeners.forEach { it1 ->
                                refreshListener(it1)
                            }
                        }
                        val listenernames: ArrayList<String> = ArrayList()
                        ActiveListeners.values.forEach { listenernames.add(it.displayname) }
                        "${ChatColor.GREEN}${listenernames.joinToString(", ")} Refresh Completed."
                    } else {
                        "Unknown Listener Name"
                    }
                } else {
                    "Wrong Arguments"
                }
            }

            "listeners" -> {
                val listeners: MutableMap<String, Int> = mutableMapOf()

                val res: ArrayList<String> = ArrayList()

                ActiveListeners.keys.forEach {
                    val name = ActiveListeners[it]!!.displayname
                    listeners[name] = 0
                    listeners[name] = listeners[name]!! + ActiveListeners[it]!!.listeners.count()
                }

                listeners.forEach { (display, size) ->
                    res.add("$display($size)")
                }

                "Listeners (${listeners.count()}):${ChatColor.GREEN} ${res.joinToString(", ")}"
            }

            "debug" -> {
                if(args.size > 1 && booleans.contains(args[1].lowercase())) {
                    isDebug = args[1].toBoolean()
                    "debug mode :" + if(isDebug) "${ChatColor.GREEN}ON" else "${ChatColor.RED}OFF"
                } else {
                    "Debug: $isDebug" + if(isDebug) {
                        var debugging: String = "\n"
                        ActiveListeners.forEach { (t, u) ->
                            var lname: String = ""
                            u.listeners.forEach {
                                lname+=it.javaClass.simpleName
                            }
                            debugging+="\n *$t* : ${u.displayname}, $lname"
                        }

                        debugging
                    } else {""}
                }
            }

            "info" -> {
                val status = getStatus()
                val content: String = """
The server is running ${status.Litewaffle.account} and is currently ${status.Litewaffle.isOnline}.
It currently has [${status.Litewaffle.listeners.joinToString(",")}] registered listeners
${status.webhooks.keys.joinToString(".\n") { key -> "the status of webhook $key is ${if(status.webhooks[key]!!) "${ChatColor.GREEN}ON" else "${ChatColor.RED}OFF"}${ChatColor.WHITE}"}}
"""

                sender.sendMessage(content)
                ""
            }

            else -> {
                "Unknown Subcommand"
            }
        }
        if (message != "${ChatColor.GOLD}[LiteWaffle] ${ChatColor.YELLOW}")
            sender.sendMessage(message)

        return false
    }
}

class CommandATComplete: TabCompleter {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String>? {
        return when(args.size) {
            1 -> LWCommandArg
            2 -> {
                when(args[0].lowercase()) {
                    "debug" -> booleans
                    "refresh" -> {
                        val listeners: MutableList<String> = ActiveListeners.keys.toMutableList()
                        listeners.add("confirm")
                        listeners
                    }
                    else -> null
                }
            }
            else -> null
        }
    }
}