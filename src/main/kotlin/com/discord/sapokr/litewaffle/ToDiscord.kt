package com.discord.sapokr.litewaffle

import club.minnced.discord.webhook.send.AllowedMentions
import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import club.minnced.discord.webhook.send.WebhookMessageBuilder
import com.discord.sapokr.litewaffle.LiteWaffle.Companion.webhooks
import net.dv8tion.jda.api.utils.MarkdownSanitizer
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

fun getAvatar(player: Player): String {
    return "https://minotar.net/helm/${player.name}"
}

class ToDiscord: Listener {
    @EventHandler
    fun onChat(event: AsyncPlayerChatEvent) {
        val message = WebhookMessageBuilder()
            .setUsername(event.player.name)
            .setAvatarUrl(getAvatar(event.player))
            .setContent(event.message)
            .setAllowedMentions(AllowedMentions.none())

        webhooks["chatLog"]!!.send(message.build())
    }
    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val message = WebhookMessageBuilder()
            .setContent(ChatColor.stripColor(event.deathMessage)?.let { MarkdownSanitizer.escape(it) })

        webhooks["killLog"]!!.send(message.build())
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {

        val embed = WebhookEmbedBuilder()
            .setAuthor(WebhookEmbed.EmbedAuthor(event.player.name, getAvatar(event.player), null))
            .setColor(0x008000)

        webhooks["chatLog"]!!.send(embed.build())
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val embed = WebhookEmbedBuilder()
            .setAuthor(WebhookEmbed.EmbedAuthor(event.player.name, getAvatar(event.player), null))
            .setColor(0xFF0000)

        webhooks["chatLog"]!!.send(embed.build())
    }
}