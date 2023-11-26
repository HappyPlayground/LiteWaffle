package com.discord.sapokr.litewaffle

import com.discord.sapokr.litewaffle.LiteWaffle.Companion.MsgListener
import com.discord.sapokr.litewaffle.LiteWaffle.Companion.Prefix
import com.discord.sapokr.litewaffle.LiteWaffle.Companion.cfg
import com.discord.sapokr.litewaffle.LiteWaffle.Companion.getStatus
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.utils.MarkdownSanitizer
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.Bukkit
import org.bukkit.ChatColor


class JDAListener: ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot || event.message.contentRaw.isEmpty()) {
            return
        }

        if (event.message.contentRaw.startsWith(Prefix)) {
            val content = event.message.contentRaw.substring(1)

            when(content.lowercase()) {
                "user", "users" -> {
                    val players = Bukkit.getOnlinePlayers()

                    val playersname = ArrayList<String>()

                    for (i in players) {
                        playersname.plus(MarkdownSanitizer.escape(i.name))
                    }

                    var playerList = "```아무도 없습니다.```"

                    if (players.size == 1) {
                        playerList = "```${MarkdownSanitizer.escape(players.last().name)}```"
                    } else if (players.isNotEmpty()) {
                        playerList = playersname.joinToString("\n", "```\n", "\n```", 10)
                    }

                    val embed = EmbedBuilder()
                        .setTitle("${players.size}명 접속중")
                        .setDescription(playerList)

                    event.message.replyEmbeds(
                        embed.build()
                    ).queue()

                    return
                }
                //TODO Will Find TPS Get
                /*"tps","틱" -> {
                    val tps = Lag.getTPS()
                    val lag = ((1.0 - tps / 20.0) * 100.0).roundToInt().toDouble()
                    val embed = EmbedBuilder()
                        .setTitle("TPS")
                        .setDescription("* TPS is calculated every 1 minute.")
                        .addField(
                            "TPS",
                            "- ${(tps * 100.0).roundToInt() / 100.0}\nlast : <t:${tpsMonitor.lastTime}:f>",
                            false
                        )
                    event.message.replyEmbeds(
                        embed.build()
                    ).queue()
                }*/
                "status" -> {
                    val status = getStatus()

                    val embed = EmbedBuilder()
                        .setTitle("Bot Status")
                        .setDescription(status.Litewaffle.isOnline)
                        .addField(
                            "Login to",
                                status.Litewaffle.account,
                            false
                        )
                        .addField(
                            "Registered listeners",
                            status.Litewaffle.listeners.joinToString { "," },
                            true
                        )
                    status.webhooks.keys.forEach {
                        embed.addField(
                            it,
                            if(status.webhooks[it]!!) "✅" else "❌",
                            true
                        )
                    }

                    event.message.replyEmbeds(
                        embed.build()
                    ).queue()
                }
            }

            if (MsgListener.size != 0) {
                MsgListener.forEach {
                    it.onMessageReceived(event)
                }
            }
            return
        }

        if (event.channel.id != cfg!!.getString("channelId")) return

        var content = event.message.contentDisplay.replace("\n", " ")

        if (event.message.stickers.isNotEmpty()) {
            content = event.message.stickers.joinToString(" ") { ":${it.name}:" } + content
        }

        if (content.isEmpty()) {
            content = "${ChatColor.RED}<내용 없음>"
        }

        val component = TextComponent("<")

        val nameComponent = TextComponent("${ChatColor.AQUA}${event.author.name}${ChatColor.RESET}")
        nameComponent.hoverEvent = HoverEvent(
            HoverEvent.Action.SHOW_TEXT,
            Text("Discord: ${event.author.name}#${event.author.discriminator}")
        )

        nameComponent.addExtra("> $content")
        component.addExtra(nameComponent)

        Bukkit.spigot().broadcast(component)
    }
}
