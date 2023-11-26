package com.discord.sapokr.litewaffle

import club.minnced.discord.webhook.send.WebhookEmbed
import club.minnced.discord.webhook.send.WebhookEmbedBuilder
import com.discord.sapokr.litewaffle.LiteWaffle.Companion.webhooks
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.plugin.Plugin
import java.io.File
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import net.dv8tion.jda.api.utils.MarkdownSanitizer

class AdvancementListener(plugin: Plugin, language: String): Listener {

    private val plugin = plugin

    private val language = language

    private val loadedLanguage: MutableMap<String, MutableMap<String, String>> = mutableMapOf()

    private fun loadLanguage(namespace: String) {
        val assetsFolder = File(plugin.dataFolder, "assets")
        val filename = if (namespace == "minecraft") {
            language
        } else {
            namespace
        }

        val jsonFile = File(assetsFolder, "$filename.json")
        val jsonMap = parseJsonStringToMap(jsonFile.readText())
        loadedLanguage[namespace] = jsonMap as MutableMap<String, String>
    }

    private fun parseJsonStringToMap(jsonStr: String): Map<String, String> {
        val gson = Gson()
        return gson.fromJson(jsonStr, object: TypeToken<Map<String, String>>() {}.type)
    }


    @EventHandler
    fun onAdvancementDone(event: PlayerAdvancementDoneEvent) {
        if (event.advancement.criteria.contains("has_the_recipe")) return
        if (!loadedLanguage.containsKey(event.advancement.key.namespace)) loadLanguage("minecraft")
        if (!loadedLanguage.containsKey(event.advancement.key.namespace)) loadLanguage(event.advancement.key.namespace)

        val title = loadedLanguage[event.advancement.key.namespace]!!["advancements." + event.advancement.key.key.replace("/", ".") + ".title"]
        var desc = loadedLanguage[event.advancement.key.namespace]!!["advancements." + event.advancement.key.key.replace("/", ".") + ".description"]

        if (desc.isNullOrBlank()) {
            if (title.isNullOrBlank()) {
                return
            } else {
                desc = "description is Blank.."
            }
        }

        val content = loadedLanguage["minecraft"]!!["chat.type.advancement.${event.advancement.display!!.type.name.lowercase()}"]!!.format(MarkdownSanitizer.escape(event.player.name), title)

        val embed = WebhookEmbedBuilder()
            .setColor(0xFFD700)
            .setTitle(
                WebhookEmbed.EmbedTitle(
                    content,
                    null
                )
            )
            .setThumbnailUrl(getAvatar(event.player))
            .setDescription(
                desc
            )

        webhooks["killLog"]!!.send(embed.build())
    }
}